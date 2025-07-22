package com.piperrideshare.driver.ui.screens.home

import android.Manifest
import android.os.Build
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
import com.piperrideshare.driver.ui.components.*
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel
import com.piperrideshare.driver.utils.LocationTracker
import com.piperrideshare.driver.utils.PermissionHandler
import kotlinx.coroutines.launch
import timber.log.Timber
import com.piperrideshare.driver.ui.components.PiperDriverMapView as ComposeMapView

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
    var rideAccepted by remember { mutableStateOf(false) }
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
                // @Thomas - BREAKPOINT HERE: Current location loaded → map will center on this position
                flyToLocation(mapView, location = location)
            }
        }
    }

    if (isOnline && currentRideRequest == null) {
        LaunchedEffect(rideRequest) {
            Timber.d("🔄 RIDE STATE: New rideRequest received = ${rideRequest?.rideId}, Current = ${currentRideRequest?.rideId}")
            // @Thomas - BREAKPOINT HERE: Triggered on every new rideRequest change while online

            if (rideRequest != null && currentRideRequest == null) {
                // @Thomas - BREAKPOINT HERE: New ride popup will show now
                currentRideRequest = rideRequest
                showRidePopup = true

                mapViewInstance?.let { mapView ->
                    rideRequest?.pickupLocation?.let { location ->
                        // @Thomas - BREAKPOINT HERE: Pickup marker added and camera moved to pickup
                        addPickupMarker(mapView, location.latitude, location.longitude)
                        flyToLocation(mapView, latitude = location.latitude, longitude = location.longitude)
                    }
                }
            } else if (rideRequest == null) {
                mapViewInstance?.let {
                    // @Thomas - BREAKPOINT HERE: No ride from server; remove pickup marker
                    clearPickupMarker()
                }
            } else {
                // @Thomas - BREAKPOINT HERE: Skipping because a ride is already active
                Timber.d("🚫 Ignoring new ride request because a ride is already in progress.")
            }
        }
    }

    fun goOnlineWithLocation() {
        coroutineScope.launch {
            val location = LocationTracker(context).getCurrentLocation()
            if (location != null) {
                // @Thomas - BREAKPOINT HERE: Successfully went online with location; socket connect happens
                currentLocation = location
                viewModel.goOnline(
                    latitude = location.first,
                    longitude = location.second,
                    zoneId = null,
                    rideTypeId = null,
                )
                isOnline = true
            } else {
                // @Thomas - BREAKPOINT HERE: Failed to get location
                Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
                pendingOnlineRequest = false
            }
        }
    }

    fun toggleOnline() {
        if (isOnline) {
            // @Thomas - BREAKPOINT HERE: Went offline manually
            currentRideRequest = null
            rideAccepted = false
            viewModel.disconnect()
            isOnline = false
        } else {
            // @Thomas - BREAKPOINT HERE: Preparing to go online — permission + location will be fetched
            viewModel.initialize()
            pendingOnlineRequest = true
        }
    }

    fun handleLogout() {
        coroutineScope.launch {
            // @Thomas - BREAKPOINT HERE: Clear Session and Logout
            viewModel.clearSession()
            viewModel.disconnect()
            onLogout()
        }
    }

    fun handleAcceptRide(rideId: String) {
        coroutineScope.launch {
            // @Thomas - BREAKPOINT HERE: Ride accepted from popup
            viewModel.acceptRide(rideId)
            // onNavigateToRideDetail(rideId)
        }
    }

    fun handleDeclineRide(rideId: String) {
        coroutineScope.launch {
            // @Thomas - BREAKPOINT HERE: Ride declined from popup
            viewModel.declineRide(rideId)
        }
    }

    if (pendingOnlineRequest) {
        val permissions =
            mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        PermissionHandler(
            permissions = permissions,
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (isOnline) {
            ComposeMapView(
                modifier = Modifier.fillMaxSize(),
                onMapReady = { mapView ->
                    mapViewInstance = mapView
                    enableLocationComponent(mapView)
                    currentLocation?.let {
                        flyToLocation(mapView, location = it)
                    }
                },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                Text(
                    "Available Ride Types: ${zone.rideTypeIds.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                )
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
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Status: Online", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (rideAccepted) "Ride accepted..." else "Waiting for ride requests...",
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        if (rideAccepted) {
                            // @Thomas - BREAKPOINT HERE: New ride received and accepted
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("🚗 New Ride ID: ${rideRequest?.rideId}", style = MaterialTheme.typography.bodyLarge)
                            PiperDriverButton(
                                text = "View Ride Details",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    Timber.d("📋 RIDE DETAILS: Navigating to ride details - ID: ${rideRequest?.rideId}")
                                    // onNavigateToRideDetail(rideRequest.rideId)
                                },
                            )
                        }
                    }
                }
            }
        }

        if (showRidePopup && currentRideRequest != null) {
            RideRequestPopup(
                rideRequest = currentRideRequest!!,
                onAccept = {
                    Timber.d("✅ POPUP ACCEPT: Accept clicked - ID: ${currentRideRequest?.rideId}")
                    // @Thomas - BREAKPOINT HERE: Ride popup accepted
                    handleAcceptRide(currentRideRequest!!.rideId)
                    showRidePopup = false
                    rideAccepted = true
                },
                onDecline = {
                    Timber.d("❌ POPUP DECLINE: Decline clicked - ID: ${currentRideRequest?.rideId}")
                    // @Thomas - BREAKPOINT HERE: Ride popup declined
                    handleDeclineRide(currentRideRequest!!.rideId)
                    showRidePopup = false
                    currentRideRequest = null
                    rideAccepted = false
                    clearPickupMarker()
                },
                onDismiss = {
                    Timber.d("🚫 POPUP DISMISS: Dismissed - ID: ${currentRideRequest?.rideId}")
                    // @Thomas - BREAKPOINT HERE: Ride popup dismissed
                    showRidePopup = false
                    currentRideRequest = null
                    clearPickupMarker()
                },
            )
        }
    }
}
