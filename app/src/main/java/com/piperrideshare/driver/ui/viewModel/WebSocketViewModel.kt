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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

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
    ) : ViewModel() {
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

        // Ride Accepted Status
        private val _rideAccepted = MutableStateFlow<Boolean>(false)
        val rideAccepted = _rideAccepted.asStateFlow()

        /**
         * Initialize WebSocket connection on ViewModel creation
         *
         * Attempts to connect to the WebSocket server using the stored authentication token.
         * This ensures real-time communication is established as soon as the driver
         * reaches the home screen.
         */
        fun initialize() {
            viewModelScope.launch {
                sessionManager.userToken.first()?.let { token ->
                    connect(token)
                }
            }
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
                    Timber.d("📨 WEBSOCKET: Message received - Type: ${response.javaClass.simpleName}")

                    // Process different types of WebSocket responses
                    when (response) {
                        // Thomas Breakpoint: Set breakpoint here to debug ride request handling
                        is RideRequestedResponse -> {
                            // @Thomas - BREAKPOINT HERE: Ride request received
                            Timber.d("🚗 WEBSOCKET: New ride request - ID: ${response.rideId}")
                            _rideRequest.value = response
                        }
                        // Thomas Breakpoint: Set breakpoint here to debug driver model updates
                        is DriverModelChangedResponse -> {
                            // @Thomas - BREAKPOINT HERE: Driver model update received
                            Timber.d("👤 WEBSOCKET: Driver model updated - ID: ${response.driverId}")
                            _driverModel.value = response
                        }
                        is RideModelChangedResponse -> {
                            // @Thomas - BREAKPOINT HERE: Ride model update received
                            Timber.d("🚕 WEBSOCKET: Ride model updated")
                            _rideModel.value = response
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
                        is ActionResponse -> {
                            // @Thomas - BREAKPOINT HERE: Action response received
                            Timber.d("📨 WEBSOCKET: Action response - ${response.action} - Status: ${response.status}")
                            if (response.error.isNotEmpty()) {
                                Timber.d("❌ WEBSOCKET: Action error - ${response.error}")
                            }

                            if (response.status == "success") {
                                when (response.action) {
                                    "accept_ride" -> _rideAccepted.value = true
                                }
                            }
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
                goOffline()
                repository.disconnect()
            }
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

                sessionManager.userToken.first()?.let { token ->
                    Timber.d("🔑 Using auth token: ${token.take(10)}...")
                    repository.sendGoOnline(latitude, longitude, deviceId, finalZoneId, finalRideTypeId)

                    // @Thomas - BREAKPOINT HERE: Go online WebSocket message sent
                    Timber.d("✅ WEBSOCKET: Go online message sent successfully")
                } ?: run {
                    // @Thomas - BREAKPOINT HERE: No auth token available
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
