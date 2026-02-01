package com.piperrideshare.driver.ui.screens.home

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapbox.maps.MapView
import com.piperrideshare.driver.api.models.DriverAvailabilityState
import com.piperrideshare.driver.api.models.DriverState
import com.piperrideshare.driver.api.models.response.websocket.RideModelChangedResponse
import com.piperrideshare.driver.api.models.response.websocket.RideRequestedResponse
import com.piperrideshare.driver.api.models.response.websocket.RiderInfoResponse
import com.piperrideshare.driver.api.models.response.websocket.ZoneInfoResponse
import com.piperrideshare.driver.ui.components.*
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * HomeTabContent - Main driver interface with state-based UI
 *
 * This replaces the complex boolean state logic with simple backend state mapping
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabContent(
    context: Context,
    viewModel: WebSocketViewModel,
    driverState: DriverState?,
    currentAvailabilityState: DriverAvailabilityState,
    zoneInfo: ZoneInfoResponse?,
    rideRequest: RideRequestedResponse?,
    rideModel: RideModelChangedResponse?,
    riderInfo: RiderInfoResponse?,
    showRidePopup: Boolean,
    showLoading: Boolean,
    showLoadingText: String,
    currentLocation: Pair<Double, Double>?,
    mapViewInstance: MapView?,
    onToggleOnline: () -> Unit,
    onAcceptRide: () -> Unit,
    onDeclineRide: () -> Unit,
    onPopupDismiss: () -> Unit,
    setMapViewInstance: (MapView) -> Unit,
    // styamamo - edit add pickupAddress and dropoffAddress as parameters
    rideRequestPickupAddress: String?,
    rideRequestDropoffAddress: String?,
) {
    // --- PIN state ---
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    // expected PIN = last 4 of riderId from rideModel
    val expectedPin = remember(rideModel?.riderId) {
        rideModel?.riderId?.takeLast(4) ?: ""
    }

    if (showPinDialog && rideModel != null) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinInput = ""
                pinError = null
            },
            title = { Text("Enter Rider PIN") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { value ->
                            // keep it to 4 digits
                            pinInput = value.filter { it.isDigit() }.take(4)
                        },
                        label = { Text("Last 4 of Rider ID") },
                        singleLine = true
                    )
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pinError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (expectedPin.isNotBlank() && pinInput == expectedPin) {
                            // PIN correct → start the ride
                            rideModel.rideId?.let { id ->
                                viewModel.startRide(id)
                            }
                            showPinDialog = false
                            pinInput = ""
                            pinError = null
                        } else {
                            pinError = "Incorrect PIN. Please try again."
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPinDialog = false
                        pinInput = ""
                        pinError = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (currentAvailabilityState in listOf(DriverAvailabilityState.EN_ROUTE, DriverAvailabilityState.ARRIVED, DriverAvailabilityState.IN_TRIP)) {
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded, skipHiddenState = true)
        )
        val coroutineScope = rememberCoroutineScope()

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                if (rideModel != null) {
                    RideAssignmentBottomSheet(
                        rideModel = rideModel,
                        riderInfo = riderInfo,
                        currentRideStatus = when (currentAvailabilityState) {
                            DriverAvailabilityState.EN_ROUTE -> "accepted"
                            DriverAvailabilityState.ARRIVED -> "driver_arrived"
                            DriverAvailabilityState.IN_TRIP -> "in_progress"
                            else -> "unknown"
                        },
                        onArriveAtPickup = { rideModel.rideId?.let { viewModel.arriveAtPickup(it) } },
                        onStartRide = {
                            // Only require PIN when driver has arrived at pickup
                            if (currentAvailabilityState == DriverAvailabilityState.ARRIVED) {
                                showPinDialog = true
                            } else {
                                // Fallback: allow start without PIN in other states if needed
                                rideModel.rideId?.let { viewModel.startRide(it) }
                            }
                        },
                        onCompleteRide = { rideModel.rideId?.let { viewModel.completeRide(it, 0.0) } },
                        onCallRider = { /* TODO */ }
                    )
                }
            },
            sheetPeekHeight = 120.dp
        ) {
            // Show loading overlay
            PiperDriverAlert(showLoading, showLoadingText)

            Box(modifier = Modifier.fillMaxSize()) {
                // Map is always shown
                MapViewContainer(
                    onMapReady = { mapView ->
                        setMapViewInstance(mapView)
                        enableLocationComponent(mapView)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 56.dp),
                )
            }
        }

        // Handle bottom sheet state based on availability state
        LaunchedEffect(currentAvailabilityState) {
            if (currentAvailabilityState in listOf(DriverAvailabilityState.EN_ROUTE, DriverAvailabilityState.ARRIVED, DriverAvailabilityState.IN_TRIP)) {
                coroutineScope.launch { scaffoldState.bottomSheetState.expand() }
            } else {
                coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
            }
        }
    } else {
        // Show the map and the online/offline UI
        Box(modifier = Modifier.fillMaxSize()) {
            // Map is always shown
            MapViewContainer(
                onMapReady = { mapView ->
                    setMapViewInstance(mapView)
                    enableLocationComponent(mapView)
                },
                modifier = Modifier
                    .fillMaxSize(),
            )

            // State-specific UI overlay
            when (currentAvailabilityState) {
                DriverAvailabilityState.OFFLINE -> {
                    OfflineStateUI(
                        zoneInfo = zoneInfo,
                        onGoOnline = onToggleOnline
                    )
                }

                DriverAvailabilityState.ONLINE -> {
                    OnlineStateUI(
                        rideRequest = rideRequest,
                        showRidePopup = showRidePopup,
                        onGoOffline = onToggleOnline,
                        onAcceptRide = onAcceptRide,
                        onDeclineRide = onDeclineRide,
                        onPopupDismiss = onPopupDismiss,
                        //styamamo - edit pass address parameters
                        pickupAddress = rideRequestPickupAddress,
                        dropoffAddress = rideRequestDropoffAddress
                    )
                }
                else -> {
                    // Do nothing
                }
            }
        }
    }

    // Handle route drawing based on state
    LaunchedEffect(currentAvailabilityState, mapViewInstance, currentLocation, rideModel) {
        if (mapViewInstance != null && currentLocation != null && rideModel != null) {
            when (currentAvailabilityState) {
                DriverAvailabilityState.EN_ROUTE -> {
                    // Draw route to pickup
                    rideModel.pickupLocation?.let { pickup ->
                        drawRouteToDestination(
                            context = context,
                            mapView = mapViewInstance,
                            destinationMarkerColor = "#FF0000",
                            currentLocation = currentLocation,
                            destinationLocation = pickup.latitude to pickup.longitude,
                        )
                    }
                }
                DriverAvailabilityState.IN_TRIP -> {
                    // Draw route to destination
                    rideModel.dropoffLocation?.let { dropoff ->
                        drawRouteToDestination(
                            context = context,
                            mapView = mapViewInstance,
                            destinationMarkerColor = "#00FF00",
                            currentLocation = currentLocation,
                            destinationLocation = dropoff.latitude to dropoff.longitude,
                        )
                    }
                }
                else -> {
                    // Clear any existing routes
                    clearPickupMarkerAndRouteLine()
                }
            }
        }
    }

    LaunchedEffect(rideModel?.riderCurrentLocation) {
        rideModel?.riderCurrentLocation?.let { riderLocation ->
            mapViewInstance?.let { mapView ->
                Timber.d("Updating rider marker to: ${riderLocation.latitude}, ${riderLocation.longitude}")
                updateRiderMarker(mapView, riderLocation.latitude, riderLocation.longitude)
            }
        }
    }
}

// ==============================================
// STATE-SPECIFIC UI COMPONENTS
// ==============================================

@Composable
private fun OfflineStateUI(
    zoneInfo: ZoneInfoResponse?,
    onGoOnline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 56.dp, vertical = 120.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PiperDriverButton(
            text = "Go Online",
            onClick = onGoOnline,
            enabled = zoneInfo != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        if (zoneInfo == null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Loading zone information...",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun OnlineStateUI(
    rideRequest: RideRequestedResponse?,
    showRidePopup: Boolean,
    onGoOffline: () -> Unit,
    onAcceptRide: () -> Unit,
    onDeclineRide: () -> Unit,
    onPopupDismiss: () -> Unit,
    //styamamo - add parameters
    pickupAddress: String?,
    dropoffAddress: String?
) {
    // Go Offline button
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 56.dp, vertical = 120.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PiperDriverButton(
            text = "Go Offline",
            onClick = onGoOffline,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
    }

    // Ride request popup
    if (showRidePopup && rideRequest != null) {
        RideRequestPopup(
            rideRequest = rideRequest,
            //styamamo - edit pass address parameters
            pickupAddress = pickupAddress,
            dropoffAddress = dropoffAddress,
            onAccept = onAcceptRide,
            onDecline = onDeclineRide,
            onDismiss = onPopupDismiss,
        )
    }
}

@Composable
private fun MapViewContainer(
    modifier: Modifier = Modifier,
    onMapReady: (MapView) -> Unit,
) {
    PiperDriverMapView(
        modifier = modifier,
        onMapReady = onMapReady,
    )
}