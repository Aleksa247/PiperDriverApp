package com.piperrideshare.driver.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.api.models.response.websocket.ActionResponse
import com.piperrideshare.driver.api.models.response.websocket.DriverModelChangedResponse
import com.piperrideshare.driver.api.models.response.websocket.RideModelChangedResponse
import com.piperrideshare.driver.api.models.response.websocket.RideRequestedResponse
import com.piperrideshare.driver.api.models.response.websocket.UnknownResponse
import com.piperrideshare.driver.api.models.response.websocket.ZoneInfoResponse
import com.piperrideshare.driver.services.IWebSocketRepository
import com.piperrideshare.driver.services.session.ISessionManager
import com.piperrideshare.driver.services.state.IDriverStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import android.content.Context
import com.piperrideshare.driver.utils.LocationTracker
import dagger.hilt.android.qualifiers.ApplicationContext
/**
 * WebSocketViewModel - Manages real-time communication for driver operations
 *
 * This ViewModel handles all WebSocket-based communication between the driver app
 * and the ride-sharing server. It manages:
 * - Real-time ride requests
 * - Driver status updates
 * - Ride state changes
 * - Location updates
 * - Session management
 *
 * The ViewModel automatically connects to WebSocket on initialization and
 * processes incoming messages to update the UI state.
 *
 * @author Thomas Woodfin
 */



@HiltViewModel
class WebSocketViewModel
    @Inject
    constructor(
        private val repository: IWebSocketRepository,
        private val sessionManager: ISessionManager,
        private val driverStateManager: IDriverStateManager,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {

        private var locationUpdateJob: Job? = null
        private var isLocationUpdatesActive = false


    // State flows for reactive UI updates
        private val _rideRequest = MutableStateFlow<RideRequestedResponse?>(null)
        val rideRequest = _rideRequest.asStateFlow()

        private val _driverModel = MutableStateFlow<DriverModelChangedResponse?>(null)
        val driverModel = _driverModel.asStateFlow()

        private val _rideModel = MutableStateFlow<RideModelChangedResponse?>(null)
        val rideModel = _rideModel.asStateFlow()

        // Zone information state
        private val _zoneInfo = MutableStateFlow<ZoneInfoResponse?>(null)
        val zoneInfo = _zoneInfo.asStateFlow()

        // Driver state flows (now managed by DriverStateManager)
        val isOnline = driverStateManager.isOnline
        val currentRideId = driverStateManager.currentRideId
        val rideStatus = driverStateManager.rideStatus

        // Ride Accepted Status
        private val _rideAccepted = MutableStateFlow<Boolean>(false)
        val rideAccepted = _rideAccepted.asStateFlow()

        // Arrived at Pickup Point
        private val _arrivedAtPickupPoint = MutableStateFlow<Boolean>(false)
        val arrivedAtPickupPoint = _arrivedAtPickupPoint.asStateFlow()

        // Ride Started Status
        private val _rideStarted = MutableStateFlow<Boolean>(false)
        val rideStarted = _rideStarted.asStateFlow()

        // Ride Completed Status
        private val _rideCompleted = MutableStateFlow<Boolean>(false)
        val rideCompleted = _rideCompleted.asStateFlow()

        // Show Loading Status
        private val _showLoading = MutableStateFlow<Boolean>(false)
        val showLoading = _showLoading.asStateFlow()

        // Show Loading Status
        private val _showLoadingText = MutableStateFlow<String>("")
        val showLoadingText = _showLoadingText.asStateFlow()

        // State restoration flag
        private val _stateRestored = MutableStateFlow<Boolean>(false)
        val stateRestored = _stateRestored.asStateFlow()


    /**
         * Initialize WebSocket connection on ViewModel creation
         *
         * Attempts to connect to the WebSocket server using the stored authentication token.
         * This ensures real-time communication is established as soon as the driver
         * reaches the home screen.
         */
        fun initialize() {
            viewModelScope.launch {

                restoreDriverState()

                sessionManager.userToken.first()?.let { token ->
                    connect(token)
                }
                startPeriodicLocationUpdates()
            }
        }

        /**
         * Sync driver state with backend driver model
         */
        private suspend fun syncDriverState(driverModel: DriverModelChangedResponse) {
            val currentOnlineState = driverStateManager.isOnline.first()
            val currentRideId = driverStateManager.currentRideId.first()

            // Sync online/offline state
            val isBackendOnline = driverModel.availabilityState.equals("ONLINE", ignoreCase = true)
            if (isBackendOnline != currentOnlineState) {
                Timber.d("🔄 SYNC: Backend availability state: ${driverModel.availabilityState}, Local: $currentOnlineState")
                Timber.d("🔄 SYNC: Updating local online state to match backend: $isBackendOnline")
                driverStateManager.setOnlineState(isBackendOnline)
            }

            // Sync current ride state
            val backendRideId = if (driverModel.currentRideID.isBlank()) null else driverModel.currentRideID
            if (backendRideId != currentRideId) {
                Timber.d("🔄 SYNC: Backend ride ID: '$backendRideId', Local: '$currentRideId'")

                if (backendRideId != null && currentRideId == null) {
                    // Backend has a ride we don't know about
                    Timber.d("🔄 SYNC: Backend has active ride '${backendRideId}' that we don't know about")
                    driverStateManager.setCurrentRide(backendRideId, "unknown")

                    // Request active ride details to get full state
                    getActiveRide()

                } else if (backendRideId == null && currentRideId != null) {
                    // We think we have a ride but backend doesn't
                    Timber.d("🔄 SYNC: Backend shows no active ride, but we have '${currentRideId}'. Clearing local state.")
                    driverStateManager.setCurrentRide(null, null)

                    // Reset ride action states
                    _rideAccepted.value = false
                    _arrivedAtPickupPoint.value = false
                    _rideStarted.value = false
                    _rideCompleted.value = false
                }
            }

            // Update location if provided
            driverModel.currentLocation?.let { location ->
                driverStateManager.updateLocation(location.latitude, location.longitude)
            }
        }


        /**
         * Sync ride state with backend model
         */
        private suspend fun syncRideState(rideModel: RideModelChangedResponse) {
            val currentRideId = driverStateManager.currentRideId.first()

            // If we have a ride in our state but backend shows different status, sync it
            if (currentRideId == rideModel.rideId) {
                when (rideModel.status.lowercase()) {
                    "accepted" -> {
                        if (!_rideAccepted.value) {
                            Timber.d("🔄 SYNC: Backend shows ride accepted, updating local state")
                            _rideAccepted.value = true
                            driverStateManager.setCurrentRide(rideModel.rideId, "accepted")
                        }
                    }
                    "driver_arrived" -> {
                        if (!_arrivedAtPickupPoint.value) {
                            Timber.d("🔄 SYNC: Backend shows driver arrived, updating local state")
                            _rideAccepted.value = false
                            _arrivedAtPickupPoint.value = true
                            driverStateManager.setCurrentRide(rideModel.rideId, "driver_arrived")
                        }
                    }
                    "in_progress" -> {
                        if (!_rideStarted.value) {
                            Timber.d("🔄 SYNC: Backend shows ride in progress, updating local state")
                            _arrivedAtPickupPoint.value = false
                            _rideStarted.value = true
                            driverStateManager.setCurrentRide(rideModel.rideId, "in_progress")
                        }
                    }
                    "completed" -> {
                        Timber.d("🔄 SYNC: Backend shows ride completed, clearing local state")
                        _rideAccepted.value = false
                        _arrivedAtPickupPoint.value = false
                        _rideStarted.value = false
                        _rideCompleted.value = true
                        driverStateManager.setCurrentRide(null, null)
                    }
                }
            }
        }

        private suspend fun restoreDriverState() {
            try {
                val state = driverStateManager.getCurrentState()
                Timber.d("STATE RESTORE: Restoring driver state - Online: ${state.isOnline}, RideId: ${state.currentRideId}")

                // Restore ride action states based on ride status
                when (state.rideStatus) {
                    "accepted" -> _rideAccepted.value = true
                    "driver_arrived" -> _arrivedAtPickupPoint.value = true
                    "in_progress" -> _rideStarted.value = true
                }

                _stateRestored.value = true

                if (state.isOnline) {
                    Timber.d("STATE RESTORE: Driver was online, will sync with backend")
                }

            } catch (e: Exception) {
                Timber.e("STATE RESTORE ERROR: ${e.message}")
                _stateRestored.value = true
            }
        }
        private suspend fun handleActionResponse(response: ActionResponse) {
            if (response.error.isNotEmpty()) {
                Timber.d("❌ WEBSOCKET: Action error - ${response.error}")
            }

            if (response.status == "success") {
                when (response.action) {
                    "go_online" -> {
                        // Online state is already saved in goOnline() method
                        Timber.d("✅ WEBSOCKET: Successfully went online")
                    }
                    "go_offline" -> {
                        driverStateManager.setOnlineState(false)
                        driverStateManager.setCurrentRide(null)
                        Timber.d("✅ WEBSOCKET: Successfully went offline")
                    }
                    "accept_ride" -> {
                        _rideAccepted.value = true
                        driverStateManager.setCurrentRide(
                            _rideRequest.value?.rideId,
                            "accepted"
                        )
                    }
                    "arrive_at_pickup" -> {
                        _arrivedAtPickupPoint.value = true
                        driverStateManager.setCurrentRide(
                            driverStateManager.currentRideId.first(),
                            "driver_arrived"
                        )
                    }
                    "start_ride" -> {
                        _rideStarted.value = true
                        driverStateManager.setCurrentRide(
                            driverStateManager.currentRideId.first(),
                            "in_progress"
                        )
                    }
                    "complete_ride" -> {
                        _rideCompleted.value = true
                        driverStateManager.setCurrentRide(null, null)
                        // Reset all ride states
                        _rideAccepted.value = false
                        _arrivedAtPickupPoint.value = false
                        _rideStarted.value = false
                    }
                }
            }
        }

        fun startPeriodicLocationUpdates() {
            if (isLocationUpdatesActive) {
                Timber.d("📍 PERIODIC: Location updates already active")
                return
            }

            locationUpdateJob = viewModelScope.launch {
                isLocationUpdatesActive = true
                val locationTracker = LocationTracker(context)

                Timber.d("📍 PERIODIC: Starting location updates every 20 seconds")

                while (isLocationUpdatesActive) {
                    try {
                        val location = locationTracker.getCurrentLocation()
                        location?.let { (lat, lng) ->
                            Timber.d("📍 PERIODIC: Sending location update - lat: $lat, lng: $lng")

                            // Send WebSocket location update
                            repository.sendUpdateLocation(lat, lng)

                            // Update persistent state
                            driverStateManager.updateLocation(lat, lng)
                        }

                        // Wait 20 seconds before next update
                        delay(20_000)

                    } catch (e: Exception) {
                        Timber.e("❌ PERIODIC LOCATION ERROR: ${e.message}")
                        // Continue the loop even if one update fails
                        delay(20_000)
                    }
                }
            }
        }

        /**
         * Stop periodic location updates
         */
        fun stopPeriodicLocationUpdates() {
            Timber.d("📍 PERIODIC: Stopping location updates")
            isLocationUpdatesActive = false
            locationUpdateJob?.cancel()
            locationUpdateJob = null
        }


    /**
         * Connect to WebSocket server with authentication token
         *
         * Establishes a persistent WebSocket connection and sets up message handling.
         * All incoming messages are processed and update the appropriate UI state.
         *
         * @param token JWT authentication token for WebSocket connection
         */
        private fun connect(token: String) {
            // O@Thomas - BREAKPOINT HERE: WebSocket connection established
            Timber.d("🔗 WEBSOCKET: Connection established with token: ${token.take(10)}...")

            repository.connect(token) { response ->
                viewModelScope.launch {
                    // Thomas Breakpoint: Set breakpoint here to see all incoming WebSocket messages
                    // @Thomas - BREAKPOINT HERE: WebSocket message received
                    _showLoading.value = false
                    Timber.d("📨 WEBSOCKET: Message received - Type: ${response.javaClass.simpleName}")

                    // Process different types of WebSocket responses
                    when (response) {
                        // Thomas Breakpoint: Set breakpoint here to debug ride request handling
                        is RideRequestedResponse -> {
                            Timber.d("🚗 WEBSOCKET: New ride request - ID: ${response.rideId}")
                            _rideRequest.value = response
                        }

                        is DriverModelChangedResponse -> {
                            Timber.d("👤 WEBSOCKET: Driver model updated - ID: ${response.driverId}")
                            Timber.d("🔄 WEBSOCKET: AvailabilityState: ${response.availabilityState}, CurrentRideID: '${response.currentRideID}'")
                            _driverModel.value = response

                            // Sync driver state with backend
                            syncDriverState(response)
                        }

                        is RideModelChangedResponse -> {
                            Timber.d("🚕 WEBSOCKET: Ride model updated - Status: ${response.status}")
                            _rideModel.value = response

                            // Sync ride state with backend
                            syncRideState(response)
                        }
                        is ZoneInfoResponse -> {
                            // @Thomas - BREAKPOINT HERE: Zone information received
                            Timber.d("🗺️ WEBSOCKET: Zone information received")
                            Timber.d("📍 Zone ID: ${response.payload.zone.id}")
                            Timber.d("🏙️ Zone Name: ${response.payload.zone.name}")
                            Timber.d("🚗 Available Ride Types: ${response.payload.zone.rideTypeIds}")
                            Timber.d("💰 Vehicle Types: ${response.payload.zone.vehicleTypes.map { it.name }}")
                            _zoneInfo.value = response
                        }
//                        is ActionResponse -> {
//                            // @Thomas - BREAKPOINT HERE: Action response received
//                            Timber.d("📨 WEBSOCKET: Action response - ${response.action} - Status: ${response.status}")
//                            if (response.error.isNotEmpty()) {
//                                Timber.d("❌ WEBSOCKET: Action error - ${response.error}")
//                            }
//
//                            if (response.status == "success") {
//                                when (response.action) {
//                                    "accept_ride" -> _rideAccepted.value = true
//                                    "arrive_at_pickup" -> _arrivedAtPickupPoint.value = true
//                                    "start_ride" -> _rideStarted.value = true
//                                    "complete_ride" -> _rideCompleted.value = true
//                                }
//                            }
//                        }
                        is ActionResponse -> {
                            Timber.d("📨 WEBSOCKET: Action response - ${response.action} - Status: ${response.status}")
                            handleActionResponse(response)
                        }

                        // Thomas Breakpoint: Set breakpoint here if getting unknown message types
                        is UnknownResponse -> {
                            // @Thomas - BREAKPOINT HERE: Unknown WebSocket message
                            Timber.d("❓ WEBSOCKET: Unknown message type - ${response.raw}")
                        }
                    }
                }
            }
        }

        /**
         * Disconnect from WebSocket server
         *
         * Called when driver goes offline or logs out to clean up
         * the WebSocket connection.
         */
        fun disconnect() {
            viewModelScope.launch {
                stopPeriodicLocationUpdates()
//                goOffline()
                repository.disconnect()
            }
        }

        override fun onCleared() {
            super.onCleared()
            stopPeriodicLocationUpdates()
        }

        /**
         * Clear session data
         *
         * Removes all stored authentication and session information.
         * Called during logout process.
         */
        suspend fun clearSession() {
            sessionManager.clearSession()
        }

        /**
         * Go online as a driver
         *
         * Sends a go online request to the server with current location
         * and driver preferences. This makes the driver available for
         * ride requests.
         *
         * @param latitude Current GPS latitude
         * @param longitude Current GPS longitude
         * @param deviceId Unique device identifier
         * @param zoneId Operating zone identifier (optional, will use received zone info if null)
         * @param rideTypeId Type of rides the driver accepts (optional, will use first available if null)
         */
        fun goOnline(
            latitude: Double,
            longitude: Double,
            zoneId: String? = null,
            rideTypeId: String? = null,
        ) {
            viewModelScope.launch {
                _showLoading.value = true
                _showLoadingText.value = "Going Online..."
                val deviceId = sessionManager.fcmToken.first() ?: "Unknown"

                // @Thomas - BREAKPOINT HERE: About to send go online WebSocket message
                Timber.d("🌐 WEBSOCKET: Sending go online request")
                Timber.d("📍 Location: lat=$latitude, lng=$longitude")
                Timber.d("📱 Device ID: $deviceId")

                // Use dynamic zone and ride type information if available
                val currentZoneInfo = _zoneInfo.value
                val finalZoneId = zoneId ?: currentZoneInfo?.payload?.zone?.id ?: "unknown_zone"
                val finalRideTypeId =
                    rideTypeId ?: currentZoneInfo
                        ?.payload
                        ?.zone
                        ?.rideTypeIds
                        ?.firstOrNull() ?: "unknown_ride_type"

                Timber.d("🗺️ Zone ID: $finalZoneId (${if (zoneId == null) "from zone info" else "provided"})")
                Timber.d("🚗 Ride Type: $finalRideTypeId (${if (rideTypeId == null) "from zone info" else "provided"})")

                // Save online state immediately
                driverStateManager.setOnlineState(
                    isOnline = true,
                    latitude = latitude,
                    longitude = longitude,
                    zoneId = finalZoneId,
                    rideTypeId = finalRideTypeId
                )

                sessionManager.userToken.first()?.let { token ->
                    repository.sendGoOnline(latitude, longitude, deviceId, finalZoneId, finalRideTypeId)
                    Timber.d("✅ WEBSOCKET: Go online message sent successfully")
                } ?: run {
                    Timber.e("❌ WEBSOCKET ERROR: No authentication token available")
                }
            }
        }

        /**
         * Update driver's current location
         *
         * Sends periodic location updates to the server for ride matching
         * and navigation purposes.
         *
         * @param latitude Current GPS latitude
         * @param longitude Current GPS longitude
         */
        fun updateLocation(
            latitude: Double,
            longitude: Double,
        ) {
            repository.sendUpdateLocation(latitude, longitude)
        }

        /**
         * Accept an incoming ride request
         *
         * @param rideId Unique identifier of the ride to accept
         */
        fun acceptRide(rideId: String) {
            viewModelScope.launch {
                // @Thomas - BREAKPOINT HERE: WebSocket accepting ride request
                // This sends the accept ride WebSocket message to the backend
                _showLoading.value = true
                _showLoadingText.value = "Matching you to the ride..."
                Timber.d("✅ WEBSOCKET: Accepting ride request - ID: $rideId")
                repository.sendAcceptRide(rideId)
                // Clear the ride request after accepting
                _rideRequest.value = null
                Timber.d("🧹 WEBSOCKET: Cleared ride request from state after accept")
            }
        }

        /**
         * Decline an incoming ride request
         *
         * @param rideId Unique identifier of the ride to decline
         */
        fun declineRide(rideId: String) {
            viewModelScope.launch {
                // @Thomas - BREAKPOINT HERE: WebSocket declining ride request
                // This handles ride decline (currently no WebSocket message, just clears state)
                Timber.e("❌ WEBSOCKET: Declining ride request - ID: $rideId")
                // TODO: Add decline ride method to repository when backend supports it
                // repository.sendDeclineRide(rideId)
                // Clear the ride request after declining
                _rideRequest.value = null
                Timber.d("🧹 WEBSOCKET: Cleared ride request from state after decline")
            }
        }

        /**
         * Notify server that driver has arrived at pickup location
         *
         * @param rideId Unique identifier of the ride
         */
        fun arriveAtPickup(rideId: String) {
            // @Thomas - BREAKPOINT HERE: Driver arriving at pickup
            // This notifies the backend that driver has arrived at pickup location
            _showLoading.value = true
            _showLoadingText.value = "Arriving at pickup point..."
            _rideAccepted.value = false
            Timber.d("📍 WEBSOCKET: Driver arriving at pickup - ID: $rideId")
            repository.sendArriveAtPickup(rideId)
        }

        /**
         * Start the ride journey
         *
         * @param rideId Unique identifier of the ride
         */
        fun startRide(rideId: String) {
            // @Thomas - BREAKPOINT HERE: Starting ride journey
            // This notifies the backend that the ride journey has started
            _showLoading.value = true
            _showLoadingText.value = "Starting the ride..."
            _arrivedAtPickupPoint.value = false
            Timber.d("🚀 WEBSOCKET: Starting ride journey - ID: $rideId")
            repository.sendStartRide(rideId)
        }

        /**
         * Complete the ride and submit final details
         *
         * @param rideId Unique identifier of the ride
         * @param distance Total distance traveled in kilometers
         */
        fun completeRide(
            rideId: String,
            distance: Double,
        ) {
            // @Thomas - BREAKPOINT HERE: Completing ride
            // This submits the final ride completion with distance traveled
            _showLoading.value = true
            _showLoadingText.value = "Completing ride..."
            _rideStarted.value = false
            Timber.d("🏁 WEBSOCKET: Completing ride - ID: $rideId, Distance: $distance km")
            repository.sendCompleteRide(rideId, distance)
        }

        /**
         * Request current active ride information
         *
         * Used to retrieve details of the ride currently in progress.
         */
        fun getActiveRide() {
            // @Thomas - BREAKPOINT HERE: Getting active ride info
            // This requests information about the currently active ride
            Timber.d("📋 WEBSOCKET: Requesting active ride information")
            repository.sendGetActiveRide()
        }

        /**
         * Request to go offline
         *
         * Used to go offline.
         */
        fun goOffline() {
            // @Thomas - BREAKPOINT HERE: Getting active ride info
            // This requests information about the currently active ride
            _showLoading.value = true
            _showLoadingText.value = "Going offline..."
            Timber.d("📋 WEBSOCKET: Go Offline")
            repository.sendGoOffline()
        }

        /**
         * Sends a WebSocket request to fetch the driver's ride history.
         *
         * This function generates a unique requestId and constructs a
         * `GetRideHistoryRequest`, which is then sent to the server.
         *
         * Expected server response: A list of previous rides.
         */
        fun goRideHistory() {
            val requestId = UUID.randomUUID().toString()
            Timber.d("📋 WEBSOCKET: Fetching ride history (requestId = $requestId)")
            repository.sendGetRideHistory(requestId)
        }
    }
