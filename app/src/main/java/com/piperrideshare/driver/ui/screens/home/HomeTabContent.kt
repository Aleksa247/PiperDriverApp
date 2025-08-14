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

/**
 * HomeTabContent - Main driver interface with state-based UI
 *
 * This replaces the complex boolean state logic with simple backend state mapping
 */
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
                    onPopupDismiss = onPopupDismiss
                )
            }

            DriverAvailabilityState.EN_ROUTE -> {
                EnRouteStateUI(
                    rideModel = rideModel,
                    riderInfo = riderInfo,
                    onArriveAtPickup = {
                        rideModel?.rideId?.let { rideId ->
                            viewModel.arriveAtPickup(rideId)
                        }
                    }
                )
            }

            DriverAvailabilityState.ARRIVED -> {
                ArrivedStateUI(
                    rideModel = rideModel,
                    riderInfo = riderInfo,
                    onStartRide = {
                        rideModel?.rideId?.let { rideId ->
                            viewModel.startRide(rideId)
                        }
                    }
                )
            }

            DriverAvailabilityState.IN_TRIP -> {
                InTripStateUI(
                    rideModel = rideModel,
                    riderInfo = riderInfo,
                    onCompleteRide = {
                        rideModel?.rideId?.let { rideId ->
                            // TODO: Calculate actual distance
                            val distance = calculateDistanceInKM(
                                rideModel.pickupLocation?.latitude ?: 0.0,
                                rideModel.pickupLocation?.longitude ?: 0.0,
                                rideModel.dropoffLocation?.latitude ?: 0.0,
                                rideModel.dropoffLocation?.longitude ?: 0.0,
                            )
                            viewModel.completeRide(rideId, distance)
                        }
                    }
                )
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
    onPopupDismiss: () -> Unit
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
            onAccept = onAcceptRide,
            onDecline = onDeclineRide,
            onDismiss = onPopupDismiss,
        )
    }
}

@Composable
private fun EnRouteStateUI(
    rideModel: RideModelChangedResponse?,
    riderInfo: RiderInfoResponse?,
    onArriveAtPickup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 56.dp, vertical = 120.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PiperDriverButton(
            text = "Arrive at Pickup",
            onClick = onArriveAtPickup,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
    }

    // Show ride assignment bottom sheet
    if (rideModel != null) {
        RideAssignmentBottomSheet(
            rideModel = rideModel,
            riderInfo = riderInfo,
            currentRideStatus = "accepted",
            onArriveAtPickup = onArriveAtPickup,
            onStartRide = { },
            onCompleteRide = { },
            onCallRider = { /* TODO: Implement call rider */ },
            onDismiss = { /* Bottom sheet is persistent */ }
        )
    }
}

@Composable
private fun ArrivedStateUI(
    rideModel: RideModelChangedResponse?,
    riderInfo: RiderInfoResponse?,
    onStartRide: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 56.dp, vertical = 120.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PiperDriverButton(
            text = "Start Ride",
            onClick = onStartRide,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
    }

    // Show ride assignment bottom sheet
    if (rideModel != null) {
        RideAssignmentBottomSheet(
            rideModel = rideModel,
            riderInfo = riderInfo,
            currentRideStatus = "driver_arrived",
            onArriveAtPickup = { },
            onStartRide = onStartRide,
            onCompleteRide = { },
            onCallRider = { /* TODO: Implement call rider */ },
            onDismiss = { /* Bottom sheet is persistent */ }
        )
    }
}

@Composable
private fun InTripStateUI(
    rideModel: RideModelChangedResponse?,
    riderInfo: RiderInfoResponse?,
    onCompleteRide: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 56.dp, vertical = 120.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PiperDriverButton(
            text = "Complete Ride",
            onClick = onCompleteRide,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800),
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
    }

    // Show ride assignment bottom sheet
    if (rideModel != null) {
        RideAssignmentBottomSheet(
            rideModel = rideModel,
            riderInfo = riderInfo,
            currentRideStatus = "in_progress",
            onArriveAtPickup = { },
            onStartRide = { },
            onCompleteRide = onCompleteRide,
            onCallRider = { /* TODO: Implement call rider */ },
            onDismiss = { /* Bottom sheet is persistent */ }
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