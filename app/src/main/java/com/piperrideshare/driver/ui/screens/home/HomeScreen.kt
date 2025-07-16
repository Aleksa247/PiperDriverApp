package com.piperrideshare.driver.ui.screens.home

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.maps.MapView
import com.piperrideshare.driver.api.models.response.websocket.RideRequestedResponse
import com.piperrideshare.driver.ui.components.PiperDriverButton
import com.piperrideshare.driver.ui.components.RideRequestPopup
import com.piperrideshare.driver.ui.map.addPickupMarker
import com.piperrideshare.driver.ui.map.clearPickupMarker
import com.piperrideshare.driver.ui.map.enableLocationComponent
import com.piperrideshare.driver.ui.map.flyToLocation
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel
import com.piperrideshare.driver.utils.LocationTracker
import com.piperrideshare.driver.utils.PermissionHandler
import kotlinx.coroutines.launch
import com.piperrideshare.driver.ui.map.MapView as ComposeMapView

@Composable
fun HomeScreen(
    onNavigateToRideDetail: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: WebSocketViewModel = hiltViewModel(),
) {
    var isOnline by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var pendingOnlineRequest by remember { mutableStateOf(false) }
    var showRidePopup by remember { mutableStateOf(false) }
    var currentRideRequest: RideRequestedResponse? = null

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Observe WebSocket state
    val rideRequest by viewModel.rideRequest.collectAsState()
    val driverModel by viewModel.driverModel.collectAsState()
    val zoneInfo by viewModel.zoneInfo.collectAsState()

    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    // Update map location when current location changes
    LaunchedEffect(currentLocation, mapViewInstance) {
        val mapView = mapViewInstance
        val location = currentLocation
        if (mapView != null && location != null) {
            flyToLocation(
                mapView = mapView,
                location = location,
            )
        }
    }

    // @Thomas - BREAKPOINT HERE: Ride request state change listener
    // This triggers whenever a new ride request comes in or is cleared
    if (isOnline) {
        LaunchedEffect(rideRequest) {
            println("🔄 RIDE STATE: Ride request state changed - Request: ${rideRequest?.rideId ?: "null"}")
            showRidePopup = rideRequest != null
            // Add pickup marker if rideRequest has pickup location
            val mapView = mapViewInstance
            val currentRideRequest = rideRequest
            if (currentRideRequest?.pickupLocation != null && mapView != null) {
                println("📍 MAP UPDATE: Adding pickup marker for ride ${currentRideRequest.rideId}")
                addPickupMarker(
                    mapView,
                    currentRideRequest.pickupLocation.latitude,
                    currentRideRequest.pickupLocation.longitude
                )
                flyToLocation(
                    mapView,
                    location = Pair(
                        currentRideRequest.pickupLocation.latitude,
                        currentRideRequest.pickupLocation.longitude
                    )
                )
            } else if (currentRideRequest == null && mapView != null) {
                println("🧹 MAP UPDATE: Clearing pickup marker - no active ride")
                clearPickupMarker()
            }
        }
    }

    /**
     * Go online with current location
     *
     * This function handles the process of going online as a driver:
     * 1. Gets current GPS location
     * 2. Sends online status to server via WebSocket
     * 3. Updates local state to reflect online status
     */
    fun goOnlineWithLocation() {
        // @Thomas - BREAKPOINT HERE: Going online with location
        // This gets the current location and sends the go online WebSocket message
        coroutineScope.launch {
            println("📍 GO ONLINE: Getting current location for go online request")
            val locationTracker = LocationTracker(context)
            val location = locationTracker.getCurrentLocation()

            if (location != null) {
                println("✅ GO ONLINE: Location obtained - lat: ${location.first}, lng: ${location.second}")
                currentLocation = location
                // Send go online request with location data
                // @Thomas - Replace deviceId with Firebase FCM token when Firebase is integrated
                // zoneId and rideTypeId are now dynamically determined from zone info received from backend
                viewModel.goOnline(
                    latitude = location.first,
                    longitude = location.second,
                    deviceId = "device123", // TODO: Replace with Firebase FCM token
                    zoneId = null, // TODO: Will use zone info from backend
                    rideTypeId = null, // TODO: Will use first available ride type from zone info
                )
                isOnline = true
                println("🟢 GO ONLINE: Driver is now online and ready for ride requests")
            } else {
                println("❌ GO ONLINE: Failed to get current location")
                Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
                pendingOnlineRequest = false
            }
        }
    }

    /**
     * Toggle online/offline status
     *
     * Handles the transition between online and offline states.
     * When going offline, disconnects from WebSocket.
     * When going online, triggers location permission check.
     */
    fun toggleOnline() {
        if (isOnline) {
            // @Thomas - BREAKPOINT HERE: Driver going offline
            // This disconnects the driver from receiving ride requests
            println("📴 DRIVER STATUS: Going offline - disconnecting from WebSocket")
            viewModel.disconnect()
            isOnline = false
        } else {
            // @Thomas - BREAKPOINT HERE: Driver going online (requesting location)
            // This triggers the location permission check and online flow
            println("📱 DRIVER STATUS: Going online - requesting location permission")
            pendingOnlineRequest = true
        }
    }

    /**
     * Handle user logout
     *
     * Clears session data, disconnects from WebSocket,
     * and navigates back to login screen.
     */
    fun handleLogout() {
        // @Thomas - BREAKPOINT HERE: Driver logout
        // This clears all session data and navigates back to login
        println("🚪 LOGOUT: Driver logging out - clearing session and disconnecting")
        coroutineScope.launch {
            viewModel.clearSession()
            viewModel.disconnect()
            onLogout()
        }
    }

    /**
     * Handle ride acceptance
     *
     * Sends accept ride request to backend and navigates to ride detail screen.
     *
     * @param rideId The ID of the ride to accept
     */
    fun handleAcceptRide(rideId: String) {
        // @Thomas - BREAKPOINT HERE: Ride accept button clicked
        // This is where the driver accepts a ride request
        println("✅ RIDE ACCEPT: Driver accepting ride - ID: $rideId")
        coroutineScope.launch {
            viewModel.acceptRide(rideId)
//            onNavigateToRideDetail(rideId)
        }
    }

    /**
     * Handle ride decline
     *
     * Sends decline ride request to backend.
     *
     * @param rideId The ID of the ride to decline
     */
    fun handleDeclineRide(rideId: String) {
        // @Thomas - BREAKPOINT HERE: Ride decline button clicked
        // This is where the driver declines a ride request
        println("❌ RIDE DECLINE: Driver declining ride - ID: $rideId")
        coroutineScope.launch {
            viewModel.declineRide(rideId)
        }
    }

    /**
     * Force refresh location
     *
     * Clears location cache and gets a fresh location to avoid "too close" errors.
     */
    fun forceRefreshLocation() {
        coroutineScope.launch {
            val locationTracker = LocationTracker(context)
            locationTracker.clearLocationCache()

            val newLocation = locationTracker.getCurrentLocation()
            if (newLocation != null) {
                currentLocation = newLocation
                Toast.makeText(context, "Location refreshed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to get new location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle location permission for going online
    if (pendingOnlineRequest) {
        PermissionHandler(
            permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION),
            onPermissionGranted = {
                pendingOnlineRequest = false
                goOnlineWithLocation()
            },
            onPermissionDenied = {
                Toast.makeText(context, "Location permission is required to go online", Toast.LENGTH_SHORT).show()
                pendingOnlineRequest = false
            },
        )
    }

    // Main UI layout
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // App title
        Text(
            text = "Driver App",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display driver ID if available
        driverModel?.driverId?.let { driverId ->
            Text("Driver ID: $driverId", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Display zone information if available
        zoneInfo?.payload?.zone?.let { zone ->
            Text("Zone: ${zone.name}", style = MaterialTheme.typography.bodyMedium)
            Text("Available Ride Types: ${zone.rideTypeIds.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Online/Offline toggle and logout buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PiperDriverButton(
                text = if (isOnline) "Go Offline" else "Go Online",
                modifier = Modifier.weight(1f),
                onClick = { toggleOnline() },
            )

            PiperDriverButton(
                text = "Logout",
                modifier = Modifier.wrapContentWidth(),
                onClick = { handleLogout() },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                    ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isOnline) {
            // Refresh location button
            PiperDriverButton(
                text = "🔄 Refresh Location",
                modifier = Modifier.fillMaxWidth(),
                onClick = { forceRefreshLocation() },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White,
                    ),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Online status card with ride request handling
        if (isOnline) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Status: Online", style = MaterialTheme.typography.bodyLarge)
                    Text("Waiting for ride requests...", style = MaterialTheme.typography.bodyMedium)

                    // Display incoming ride request if available
                    rideRequest?.let { request ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("🚗 New Ride ID: ${request.rideId}", style = MaterialTheme.typography.bodyLarge)
                        PiperDriverButton(
                            text = "View Ride Details",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                // @Thomas - BREAKPOINT HERE: View ride details button clicked
                                // This navigates to the ride detail screen
                                println("📋 RIDE DETAILS: Navigating to ride details - ID: ${request.rideId}")
//                                onNavigateToRideDetail(request.rideId)
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Show ride request popup if available
        // @Thomas - BREAKPOINT HERE: Ride popup rendering
        // This controls when the ride request popup is displayed
        if (currentRideRequest == null) {
            currentRideRequest = rideRequest
        }

        if (showRidePopup && currentRideRequest != null && currentRideRequest == rideRequest) {
            println("🪟 RIDE POPUP: Displaying ride request popup - ID: ${currentRideRequest.rideId}")
            RideRequestPopup(
                rideRequest = currentRideRequest,
                onAccept = {
                    // @Thomas - BREAKPOINT HERE: Ride popup accept callback
                    // This is triggered when driver clicks Accept in the popup
                    println("✅ POPUP ACCEPT: Accept clicked in popup - ID: ${currentRideRequest?.rideId}")
                    handleAcceptRide(currentRideRequest!!.rideId)
                    showRidePopup = false
                    clearPickupMarker()
                },
                onDecline = {
                    // @Thomas - BREAKPOINT HERE: Ride popup decline callback
                    // This is triggered when driver clicks Decline in the popup
                    println("❌ POPUP DECLINE: Decline clicked in popup - ID: ${currentRideRequest?.rideId}")
                    handleDeclineRide(currentRideRequest!!.rideId)
                    showRidePopup = false
                    clearPickupMarker()
                },
                onDismiss = {
                    // TODO: @Thomas - BREAKPOINT HERE: Ride popup dismiss callback
                    // This is triggered when driver dismisses the popup without action
                    println("🚫 POPUP DISMISS: Popup dismissed - ID: ${currentRideRequest?.rideId}")
                    showRidePopup = false
                    clearPickupMarker()
                },
            )
        }

        // Map view for location display (only shown when online)
        if (isOnline) {
            ComposeMapView(
                onMapReady = { mapView ->
                    mapViewInstance = mapView
                    enableLocationComponent(mapView)
                    currentLocation?.let {
                        flyToLocation(mapView, location = it)
                    }
                },
            )
        }
    }
}
