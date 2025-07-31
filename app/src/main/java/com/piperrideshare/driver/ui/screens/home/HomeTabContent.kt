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
import com.piperrideshare.driver.api.models.response.websocket.RideRequestedResponse
import com.piperrideshare.driver.ui.components.*
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel

@Composable
fun HomeTabContent(
    context: Context,
    viewModel: WebSocketViewModel,
    showOnlineOfflineToggleButton: Boolean,
    isOnline: Boolean,
    onToggleOnline: () -> Unit,
    onAcceptRide: () -> Unit,
    onDeclineRide: () -> Unit,
    currentRideRequest: RideRequestedResponse?,
    showRidePopup: Boolean,
    onPopupDismiss: () -> Unit,
    setShowRidePopup: (Boolean) -> Unit,
    setCurrentRideRequest: (RideRequestedResponse?) -> Unit,
    mapViewInstance: MapView?,
    setMapViewInstance: (MapView) -> Unit,
    currentLocation: Pair<Double, Double>?,
    showLoading: Boolean,
    showLoadingText: String,
    rideAccepted: Boolean,
    arrivedAtPickupPoint: Boolean,
    rideStarted: Boolean,
    rideCompleted: Boolean,
) {
    PiperDriverAlert(
        showLoading,
        showLoadingText,
    )

    if (rideCompleted) {
        clearPickupMarkerAndRouteLine()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapViewContainer(
            onMapReady = { mapView ->
                setMapViewInstance(mapView)
                enableLocationComponent(mapView)
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp),
        )

        when {
            rideAccepted || arrivedAtPickupPoint || rideStarted -> {
                RideActionButton(
                    rideAccepted = rideAccepted,
                    arrivedAtPickupPoint = arrivedAtPickupPoint,
                    viewModel = viewModel,
                    currentRideRequest = currentRideRequest,
                )
            }
            showOnlineOfflineToggleButton -> {
                OnlineToggleSection(
                    isOnline = isOnline,
                    onToggle = onToggleOnline,
                )
            }
        }

        if (showRidePopup && currentRideRequest != null) {
            RideRequestPopup(
                rideRequest = currentRideRequest,
                onAccept = {
                    onAcceptRide()
                    setShowRidePopup(false)
                },
                onDecline = {
                    onDeclineRide()
                    setShowRidePopup(false)
                    onPopupDismiss()
                },
                onDismiss = {
                    setShowRidePopup(false)
                    onPopupDismiss()
                },
            )
        }
    }

    LaunchedEffect(mapViewInstance, currentLocation) {
        mapViewInstance?.let { map ->
            currentLocation?.let { (lat, lon) ->
                flyToLocation(map, lat, lon)
            }
        }
    }

    LaunchedEffect(rideAccepted, arrivedAtPickupPoint, mapViewInstance, currentLocation, currentRideRequest) {
        if ((rideAccepted || arrivedAtPickupPoint) &&
            mapViewInstance != null &&
            currentLocation != null &&
            currentRideRequest != null
        ) {
            val destination =
                if (rideAccepted) {
                    currentRideRequest.pickupLocation
                } else {
                    currentRideRequest.dropoffLocation
                }

            destination?.latitude?.let { destLat ->
                destination.longitude?.let { destLng ->
                    drawRouteToDestination(
                        context = context,
                        mapView = mapViewInstance,
                        destinationMarkerColor = "#FF0000",
                        currentLocation = currentLocation,
                        destinationLocation = destLat to destLng,
                    )
                }
            }
        }
    }
}

@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    onMapReady: (MapView) -> Unit,
) {
    PiperDriverMapView(
        modifier = modifier,
        onMapReady = onMapReady,
    )
}

@Composable
fun OnlineToggleSection(
    isOnline: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 56.dp, vertical = 120.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnlineToggleButton(
            isOnline = isOnline,
            onToggle = onToggle,
        )
    }
}

@Composable
fun OnlineToggleButton(
    isOnline: Boolean,
    onToggle: () -> Unit,
) {
    val buttonText = if (isOnline) "Go Offline" else "Go Online"
    val buttonColor = if (isOnline) Color.Gray else MaterialTheme.colorScheme.primary

    PiperDriverButton(
        text = buttonText,
        onClick = onToggle,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    )
}

@Composable
fun RideActionButton(
    rideAccepted: Boolean,
    arrivedAtPickupPoint: Boolean,
    viewModel: WebSocketViewModel,
    currentRideRequest: RideRequestedResponse?,
) {
    val buttonText =
        when {
            rideAccepted -> "Arrive at Pickup Point"
            arrivedAtPickupPoint -> "Start Ride"
            else -> "Complete Ride"
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 56.dp, vertical = 120.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PiperDriverButton(
            text = buttonText,
            onClick = {
                currentRideRequest?.rideId?.let { rideId ->
                    when {
                        rideAccepted -> viewModel.arriveAtPickup(rideId)
                        arrivedAtPickupPoint -> viewModel.startRide(rideId)
                        else ->
                            viewModel.completeRide(
                                rideId,
                                calculateDistanceInKM(
                                    currentRideRequest.pickupLocation!!.latitude,
                                    currentRideRequest.pickupLocation.longitude,
                                    currentRideRequest.dropoffLocation!!.latitude,
                                    currentRideRequest.dropoffLocation.longitude,
                                ),
                            )
                    }
                }
            },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        )
    }
}
