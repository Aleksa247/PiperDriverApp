package com.piperrideshare.driver.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.api.models.DriverAvailabilityState
import com.piperrideshare.driver.api.models.DriverState
import com.piperrideshare.driver.api.models.response.websocket.ActionResponse
import com.piperrideshare.driver.api.models.response.websocket.DriverModelChangedResponse
import com.piperrideshare.driver.api.models.response.websocket.EarningsResponse
import com.piperrideshare.driver.api.models.response.websocket.ProfileResponse
import com.piperrideshare.driver.api.models.response.websocket.RideModelChangedResponse
import com.piperrideshare.driver.api.models.response.websocket.RideRequestedResponse
import com.piperrideshare.driver.api.models.response.websocket.RiderInfoResponse
import com.piperrideshare.driver.api.models.response.websocket.RideHistoryResponse
import com.piperrideshare.driver.api.models.response.websocket.UnknownResponse
import com.piperrideshare.driver.api.models.response.websocket.ZoneInfoResponse
import com.piperrideshare.driver.api.models.toDriverState
import com.piperrideshare.driver.services.IWebSocketRepository
import com.piperrideshare.driver.services.session.ISessionManager
import com.piperrideshare.driver.services.IDriverStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import android.content.Context
import com.piperrideshare.driver.utils.LocationTracker
import com.piperrideshare.driver.services.DynamicLocationUpdateManager
import com.piperrideshare.driver.services.MapboxSearchService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Simplified WebSocketViewModel - Single Source of Truth from Backend
 *
 * REMOVED: Multiple boolean flags (_rideAccepted, _arrivedAtPickupPoint, etc.)
 * ADDED: Single backend state management + Task 2.1 features
 *
 * The backend sends driver_model_changed events that contain the definitive state.
 * This ViewModel just reflects that state and sends commands.
 */
@HiltViewModel
class WebSocketViewModel
@Inject
constructor(
    private val repository: IWebSocketRepository,
    private val sessionManager: ISessionManager,
    private val driverStateManager: IDriverStateManager,
    private val dynamicLocationManager: DynamicLocationUpdateManager,
    private val mapboxSearchService: MapboxSearchService,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // ==============================================
    // BACKEND STATE - Single Source of Truth
    // ==============================================

    // Use the DriverStateManager as the single source of truth
    val driverState = driverStateManager.driverState

    // Convenience flows derived from backend state
    val availabilityState = driverStateManager.availabilityState
    val currentRideId = driverStateManager.currentRideId
    val isOnline = driverStateManager.driverState.map { it?.availabilityState == DriverAvailabilityState.ONLINE }

    // ==============================================
    // OTHER WEBSOCKET DATA
    // ==============================================

    private val _rideRequest = MutableStateFlow<RideRequestedResponse?>(null)
    val rideRequest = _rideRequest.asStateFlow()

    private val _rideModel = MutableStateFlow<RideModelChangedResponse?>(null)
    val rideModel = _rideModel.asStateFlow()

    private val _zoneInfo = MutableStateFlow<ZoneInfoResponse?>(null)
    val zoneInfo = _zoneInfo.asStateFlow()

    private val _riderInfo = MutableStateFlow<RiderInfoResponse?>(null)
    val riderInfo = _riderInfo.asStateFlow()

    private val _rideHistory = MutableStateFlow<RideHistoryResponse?>(null)
    val rideHistory = _rideHistory.asStateFlow()

    private val _rideRequestPickupAddress = MutableStateFlow<String?>(null)
    val rideRequestPickupAddress: StateFlow<String?> = _rideRequestPickupAddress

    private val _rideRequestDropoffAddress = MutableStateFlow<String?>(null)
    val rideRequestDropoffAddress: StateFlow<String?> = _rideRequestDropoffAddress

    // ==============================================
    // NEW STATE FOR TASK 2.1
    // ==============================================

    private val _profileResponse = MutableStateFlow<ProfileResponse?>(null)
    val profileResponse = _profileResponse.asStateFlow()

    private val _earningsResponse = MutableStateFlow<EarningsResponse?>(null)
    val earningsResponse = _earningsResponse.asStateFlow()

    // ==============================================
    // UI STATE (NOT BUSINESS LOGIC STATE)
    // ==============================================

    private val _showLoading = MutableStateFlow<Boolean>(false)
    val showLoading = _showLoading.asStateFlow()

    private val _showLoadingText = MutableStateFlow<String>("")
    val showLoadingText = _showLoadingText.asStateFlow()

    private val _stateRestored = MutableStateFlow<Boolean>(false)
    val stateRestored = _stateRestored.asStateFlow()

    // ==============================================
    // INITIALIZATION
    // ==============================================

    fun initialize() {
        viewModelScope.launch {
            // Restore state from persistent storage
            restoreDriverState()

            // Connect to WebSocket
            sessionManager.userToken.first()?.let { token ->
                connect(token)
            }

            // Start dynamic location updates
            startDynamicLocationUpdates()
        }
    }

    private suspend fun restoreDriverState() {
        try {
            val state = driverStateManager.getCurrentState()
            state?.let {
                Timber.d("STATE RESTORE: Restored driver state - ${it.availabilityState}, RideId: ${it.currentRideId}")
                // Initialize dynamic location manager with restored state
                dynamicLocationManager.updateDriverState(it.availabilityState)
            }
            _stateRestored.value = true
        } catch (e: Exception) {
            Timber.e("STATE RESTORE ERROR: ${e.message}")
            _stateRestored.value = true
        }
    }

    // ==============================================
    // WEBSOCKET MESSAGE HANDLING
    // ==============================================

    private fun connect(token: String) {
        // NOTE: H3 header is REQUIRED for driver WebSocket to work (backend returns 400 without it)
        Timber.d("🔗 WEBSOCKET: Connection established with token: ${token.take(10)}...")

        viewModelScope.launch {
            repository.connect(token) { response ->
                viewModelScope.launch {
                    _showLoading.value = false
                    Timber.d("📨 WEBSOCKET: Message received - Type: ${response.javaClass.simpleName}")

                    when (response) {
                        is DriverModelChangedResponse -> {
                            handleDriverModelChanged(response)
                        }

                        is RideModelChangedResponse -> {
                            // Fetch addresses for display using MapboxSearchService
                            Timber.d("🚕 WEBSOCKET: Ride model updated - Status: ${response.status}")
                            _rideModel.value = response

                            viewModelScope.launch(Dispatchers.IO) {
                                // Pickup address
                                val pickupAddress = response.pickupLocation?.let {
                                    mapboxSearchService.reverseGeocode(it.latitude, it.longitude)
                                }
                                // Dropoff address
                                val dropoffAddress = response.dropoffLocation?.let {
                                    mapboxSearchService.reverseGeocode(it.latitude, it.longitude)
                                }
                                // Update ride model with addresses
                                if (_rideModel.value?.rideId == response.rideId) {
                                    _rideModel.value = _rideModel.value?.copy(
                                        pickupAddress = pickupAddress,
                                        dropoffAddress = dropoffAddress
                                    )
                                    Timber.d("🚕 WEBSOCKET: Addresses fetched: Pickup: $pickupAddress, Dropoff: $dropoffAddress")
                                }
                            }

                            // Request rider info if we don't have it yet and have a valid ride
                            if (_riderInfo.value == null && response.riderId.isNotBlank()) {
                                repository.sendGetRiderInfo(response.rideId, response.riderId)
                            }
                        }

                        is RideRequestedResponse -> {
                            _rideRequest.value = response
                            Timber.d("🚗 WEBSOCKET: New ride request - ID: ${response.rideId}")
                        }

                        is ZoneInfoResponse -> {
                            _zoneInfo.value = response
                            Timber.d("🗺️ WEBSOCKET: Zone info received - ${response.payload.zone.name}")
                        }

                        is RiderInfoResponse -> {
                            _riderInfo.value = response
                            Timber.d("👤 WEBSOCKET: Rider info received")
                        }

                        is ActionResponse -> {
                            handleActionResponse(response)
                        }

                        // ==============================================
                        // NEW RESPONSE HANDLERS FOR TASK 2.1
                        // ==============================================

                        is ProfileResponse -> {
                            _profileResponse.value = response
                            Timber.d("📋 WEBSOCKET: Profile response received")
                        }

                        is EarningsResponse -> {
                            _earningsResponse.value = response
                            Timber.d("💰 WEBSOCKET: Earnings response received")
                        }

                        is RideHistoryResponse -> {
                            _rideHistory.value = response
                            Timber.d("📜 WEBSOCKET: Ride history response received")
                        }

                        is UnknownResponse -> {
                            Timber.d("❓ WEBSOCKET: Unknown message - ${response.raw}")
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle driver model changes from backend - This is the SINGLE SOURCE OF TRUTH
     */
    private suspend fun handleDriverModelChanged(response: DriverModelChangedResponse) {
        Timber.d("👤 WEBSOCKET: Driver model changed")
        Timber.d("📊 BACKEND STATE: ${response.availabilityState}, RideID: '${response.currentRideID}'")

        // Convert backend response to local state
        val newDriverState = response.toDriverState()

        // Persist state (this will automatically update all flows observing driverStateManager.driverState)
        driverStateManager.saveDriverState(newDriverState)
        
        // Update dynamic location manager with new driver state
        dynamicLocationManager.updateDriverState(newDriverState.availabilityState)
        
        // Clear rider info and ride model if driver goes back to ONLINE (ride completed/cancelled)
        if (newDriverState.availabilityState == DriverAvailabilityState.ONLINE && 
            newDriverState.currentRideId.isNullOrBlank()) {
            _riderInfo.value = null
            _rideModel.value = null
        }
    }

    /**
     * Handle action responses - just show loading states
     */
    private suspend fun handleActionResponse(response: ActionResponse) {
        _showLoading.value = false

        if (response.status == "success") {
            Timber.d("✅ WEBSOCKET: ${response.action} succeeded")
        } else {
            Timber.e("❌ WEBSOCKET: ${response.action} failed - ${response.error}")
        }
    }

    // ==============================================
    // COMMAND METHODS - Send to Backend
    // ==============================================

    fun goOnline(latitude: Double, longitude: Double, zoneId: String, rideTypeId: String) {
        viewModelScope.launch {
            _showLoading.value = true
            _showLoadingText.value = "Going Online..."

            val deviceId = sessionManager.fcmToken.first() ?: "Unknown"

            Timber.d("🌐 COMMAND: Going online at $latitude, $longitude")

            // Save preferences for next time
            driverStateManager.saveOnlinePreferences(zoneId, rideTypeId)

            repository.sendGoOnline(latitude, longitude, deviceId, zoneId, rideTypeId)
        }
    }

    fun goOffline() {
        viewModelScope.launch {
            _showLoading.value = true
            _showLoadingText.value = "Going offline..."

            Timber.d("📴 COMMAND: Going offline")
            repository.sendGoOffline()
        }
    }

    fun acceptRide(rideId: String) {
        viewModelScope.launch {
            _showLoading.value = true
            _showLoadingText.value = "Accepting ride..."

            Timber.d("✅ COMMAND: Accepting ride $rideId")
            repository.sendAcceptRide(rideId)
            _rideRequest.value = null // Clear the request
        }
    }

    fun arriveAtPickup(rideId: String) {
        viewModelScope.launch {
            _showLoading.value = true
            _showLoadingText.value = "Arriving at pickup..."

            Timber.d("📍 COMMAND: Arriving at pickup for $rideId")
            repository.sendArriveAtPickup(rideId)
        }
    }

    fun startRide(rideId: String) {
        viewModelScope.launch {
            _showLoading.value = true
            _showLoadingText.value = "Starting ride..."

            Timber.d("🚀 COMMAND: Starting ride $rideId")
            repository.sendStartRide(rideId)
        }
    }

    fun completeRide(rideId: String, distance: Double) {
        viewModelScope.launch {
            _showLoading.value = true
            _showLoadingText.value = "Completing ride..."

            Timber.d("🏁 COMMAND: Completing ride $rideId")
            repository.sendCompleteRide(rideId, distance)
        }
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        repository.sendUpdateLocation(latitude, longitude)
    }

    /**
     * Request ride history
     */
    fun goRideHistory() {
        val requestId = UUID.randomUUID().toString()
        Timber.d("📋 WEBSOCKET: Fetching ride history (requestId = $requestId)")
        repository.sendGetRideHistory(requestId)
    }

    // ==============================================
    // NEW METHODS FOR TASK 2.1
    // ==============================================

    /**
     * Request driver profile information
     */
    fun getProfile() {
        val requestId = UUID.randomUUID().toString()
        Timber.d("📋 WEBSOCKET: Requesting profile (requestId = $requestId)")
        repository.sendGetProfile(requestId)
    }

    /**
     * Request driver earnings information
     *
     * @param timeFrame "week", "month", or "all"
     */
    fun getEarnings(timeFrame: String = "week") {
        val requestId = UUID.randomUUID().toString()
        Timber.d("💰 WEBSOCKET: Requesting earnings for $timeFrame (requestId = $requestId)")
        repository.sendGetEarnings(requestId, timeFrame)
    }



    // ==============================================
    // DYNAMIC LOCATION UPDATES
    // ==============================================

    fun startDynamicLocationUpdates() {
        dynamicLocationManager.startLocationUpdates(viewModelScope) { lat, lng ->
            // Send location to backend
            repository.sendUpdateLocation(lat, lng)
            // Update local state
            viewModelScope.launch {
                driverStateManager.updateLocation(lat, lng)
            }
        }
    }

    fun stopDynamicLocationUpdates() {
        dynamicLocationManager.stopLocationUpdates()
    }

    // ==============================================
    // CLEANUP
    // ==============================================

    fun disconnect() {
        viewModelScope.launch {
            stopDynamicLocationUpdates()
            repository.disconnect()
        }
    }

    suspend fun clearSession() {
        sessionManager.clearSession()
        driverStateManager.clearState()
    }

    override fun onCleared() {
        super.onCleared()
        stopDynamicLocationUpdates()
    }
}