package com.piperrideshare.driver.ui.screens.home

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.piperrideshare.driver.ui.map.*
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
    var currentRideRequest by remember { mutableStateOf<RideRequestedResponse?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val rideRequest by viewModel.rideRequest.collectAsState()
    val driverModel by viewModel.driverModel.collectAsState()
    val zoneInfo by viewModel.zoneInfo.collectAsState()
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(currentLocation, mapViewInstance) {
        currentLocation?.let { location ->
            mapViewInstance?.let { mapView ->
                flyToLocation(mapView, location = location)
            }
        }
    }

    if (isOnline && currentRideRequest == null) {
        LaunchedEffect(rideRequest) {
            println("🔄 RIDE STATE: New rideRequest received = ${rideRequest?.rideId}, Current = ${currentRideRequest?.rideId}")

            if (rideRequest != null && currentRideRequest == null) {
                // Only accept if no ride is active
                currentRideRequest = rideRequest
                showRidePopup = true

                mapViewInstance?.let { mapView ->
                    rideRequest?.pickupLocation?.let { location ->
                        addPickupMarker(mapView, location.latitude, location.longitude)
                        flyToLocation(mapView, latitude = location.latitude, longitude = location.longitude)
                    }
                }
            } else if (rideRequest == null) {
                // If rideRequest is null (e.g., cleared by server), remove marker
                mapViewInstance?.let {
                    clearPickupMarker()
                }
            } else {
                println("🚫 Ignoring new ride request because a ride is already in progress.")
            }
        }
    }

    fun goOnlineWithLocation() {
        coroutineScope.launch {
            val location = LocationTracker(context).getCurrentLocation()
            if (location != null) {
                currentLocation = location
                viewModel.goOnline(
                    latitude = location.first,
                    longitude = location.second,
                    deviceId = "device123",
                    zoneId = null,
                    rideTypeId = null,
                )
                isOnline = true
            } else {
                Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
                pendingOnlineRequest = false
            }
        }
    }

    fun toggleOnline() {
        if (isOnline) {
            viewModel.disconnect()
            isOnline = false
        } else {
            pendingOnlineRequest = true
            currentRideRequest = null
        }
    }

    fun handleLogout() {
        coroutineScope.launch {
            viewModel.clearSession()
            viewModel.disconnect()
            onLogout()
        }
    }

    fun handleAcceptRide(rideId: String) {
        coroutineScope.launch {
            viewModel.acceptRide(rideId)
            // onNavigateToRideDetail(rideId)
        }
    }

    fun handleDeclineRide(rideId: String) {
        coroutineScope.launch {
            viewModel.declineRide(rideId)
        }
    }

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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Driver App", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))

        driverModel?.driverId?.let {
            Text("Driver ID: $it", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        zoneInfo?.payload?.zone?.let { zone ->
            Text("Zone: ${zone.name}", style = MaterialTheme.typography.bodyMedium)
            Text("Available Ride Types: ${zone.rideTypeIds.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
        }

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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White,
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isOnline) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Status: Online", style = MaterialTheme.typography.bodyLarge)
                    Text("Waiting for ride requests...", style = MaterialTheme.typography.bodyMedium)

                    // Show ride info only if there's no current ride
                    if (rideRequest != null && currentRideRequest == null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("🚗 New Ride ID: ${rideRequest?.rideId}", style = MaterialTheme.typography.bodyLarge)
                        PiperDriverButton(
                            text = "View Ride Details",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                println("📋 RIDE DETAILS: Navigating to ride details - ID: ${rideRequest?.rideId}")
//            onNavigateToRideDetail(rideRequest.rideId)
                            },
                        )
                    } else if (rideRequest != null && currentRideRequest != null) {
                        println("🚫 Ignored New Ride ID: ${rideRequest?.rideId} — already handling ride ${currentRideRequest?.rideId}")
                    }

                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showRidePopup && currentRideRequest != null && currentRideRequest == rideRequest) {
            RideRequestPopup(
                rideRequest = currentRideRequest!!,
                onAccept = {
                    println("✅ POPUP ACCEPT: Accept clicked - ID: ${currentRideRequest?.rideId}")
                    handleAcceptRide(currentRideRequest!!.rideId)
                    showRidePopup = false
                    // Do not clear currentRideRequest
                },
                onDecline = {
                    println("❌ POPUP DECLINE: Decline clicked - ID: ${currentRideRequest?.rideId}")
                    handleDeclineRide(currentRideRequest!!.rideId)
                    showRidePopup = false
                    currentRideRequest = null
                    clearPickupMarker()
                },
                onDismiss = {
                    println("🚫 POPUP DISMISS: Dismissed - ID: ${currentRideRequest?.rideId}")
                    showRidePopup = false
                    currentRideRequest = null
                    clearPickupMarker()
                },
            )
        }

        if (isOnline) {
            ComposeMapView(
                onMapReady = { mapView ->
                    mapViewInstance = mapView
                    enableLocationComponent(mapView)
                    currentLocation?.let { flyToLocation(mapView, location = it) }
                },
            )
        }
    }
}
