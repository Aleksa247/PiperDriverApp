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
    rideAccepted: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen Map
        MapViewContainer(
            onMapReady = { mapView ->
                setMapViewInstance(mapView)
                enableLocationComponent(mapView)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp)
        )

        if (rideAccepted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 56.dp)
                    .padding(bottom = 120.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PiperDriverButton(
                    text = "Arrive at Pickup Point",
                    onClick = {
                        viewModel.arriveAtPickup(currentRideRequest!!.rideId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        } else if (showOnlineOfflineToggleButton) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 56.dp)
                    .padding(bottom = 120.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnlineToggleButton(
                    isOnline = isOnline,
                    onToggle = onToggleOnline
                )
            }
        }

        // Ride request popup
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
                }
            )
        }
    }

    // Fly to location when mapViewInstance and currentLocation are available
    LaunchedEffect(mapViewInstance, currentLocation) {
        val map = mapViewInstance
        val location = currentLocation
        if (map != null && location != null) {
            val (lat, lon) = location
            flyToLocation(map, lat, lon)
        }
    }

    LaunchedEffect(rideAccepted, mapViewInstance, currentLocation, currentRideRequest) {
        if (
            rideAccepted &&
            mapViewInstance != null &&
            currentLocation != null &&
            currentRideRequest != null
        ) {
            val destination = currentRideRequest.pickupLocation
            val destinationLat = destination?.latitude
            val destinationLng = destination?.longitude

            if (destinationLat != null && destinationLng != null) {
                drawRouteToDestination(
                    context = context,
                    mapView = mapViewInstance,
                    destinationMarkerColor = "#FF0000",
                    currentLocation = currentLocation,
                    destinationLocation = destinationLat to destinationLng
                )
            }
        }
    }
}

@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    onMapReady: (MapView) -> Unit
) {
    PiperDriverMapView(
        modifier = modifier,
        onMapReady = onMapReady
    )
}

@Composable
fun OnlineToggleButton(
    isOnline: Boolean,
    onToggle: () -> Unit
) {
    val buttonText = if (isOnline) "Go Offline" else "Go Online"
    val buttonColor = if (isOnline) Color.Gray else MaterialTheme.colorScheme.primary

    PiperDriverButton(
        text = buttonText,
        onClick = onToggle,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}
