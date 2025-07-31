package com.piperrideshare.driver.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.maps.MapView
import com.piperrideshare.driver.api.models.response.websocket.RideRequestedResponse
import com.piperrideshare.driver.ui.components.clearPickupMarkerAndRouteLine
import com.piperrideshare.driver.ui.components.flyToLocation
import com.piperrideshare.driver.ui.screens.account.AccountScreen
import com.piperrideshare.driver.ui.screens.activity.ActivityScreen
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel
import com.piperrideshare.driver.utils.LocationTracker
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class BottomNavItem(val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("Home", Icons.Default.Home)
    object Activity : BottomNavItem("Activity", Icons.AutoMirrored.Filled.List)
    object Account : BottomNavItem("Account", Icons.Default.Person)
}

@Composable
fun HomeScreen(
    onNavigateToRideDetail: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: WebSocketViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Use persistent state from ViewModel instead of local state
    val isOnline by viewModel.isOnline.collectAsState(initial = false)
    val currentRideIdFromState by viewModel.currentRideId.collectAsState(initial = null)
    val rideStatus by viewModel.rideStatus.collectAsState(initial = null)
    val stateRestored by viewModel.stateRestored.collectAsState(initial = false)

    // Local UI state
    var currentLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showRidePopup by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var currentRideRequest by remember { mutableStateOf<RideRequestedResponse?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var lastZoneName by remember { mutableStateOf<String?>(null) }

    // WebSocket state
    val rideRequest by viewModel.rideRequest.collectAsState()
    val driverModel by viewModel.driverModel.collectAsState()
    val zoneInfo by viewModel.zoneInfo.collectAsState()
    val rideAccepted by viewModel.rideAccepted.collectAsState()
    val arrivedAtPickupPoint by viewModel.arrivedAtPickupPoint.collectAsState()
    val rideStarted by viewModel.rideStarted.collectAsState()
    val rideCompleted by viewModel.rideCompleted.collectAsState()
    val showLoading by viewModel.showLoading.collectAsState()
    val showLoadingText by viewModel.showLoadingText.collectAsState()

    var selectedTab by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Home) }


    // Show state restoration info to user
    LaunchedEffect(stateRestored, isOnline, currentRideIdFromState, rideStatus) {
        if (stateRestored) {
            when {
                isOnline && currentRideIdFromState != null -> {
                    val statusMessage = when (rideStatus) {
                        "accepted" -> "You have an accepted ride. Continue to pickup."
                        "driver_arrived" -> "You're at pickup. Ready to start the ride."
                        "in_progress" -> "Ride in progress. Continue to destination."
                        else -> "You have an active ride."
                    }
                    Toast.makeText(context, "🔄 State restored: $statusMessage", Toast.LENGTH_LONG).show()
                }
                isOnline -> {
                    Toast.makeText(context, "🟢 You're back online and ready for rides!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Show driver model synchronization feedback
    LaunchedEffect(driverModel) {
        driverModel?.let { model ->
            val backendOnline = model.availabilityState.equals("ONLINE", ignoreCase = true)
            val hasBackendRide = model.currentRideID.isNotBlank()

            when {
                backendOnline && !isOnline -> {
                    Toast.makeText(context, "🔄 Synced: You're now online (restored from backend)", Toast.LENGTH_SHORT).show()
                }
                !backendOnline && isOnline -> {
                    Toast.makeText(context, "🔄 Synced: You're now offline (backend timeout)", Toast.LENGTH_SHORT).show()
                }
                hasBackendRide && currentRideIdFromState == null -> {
                    Toast.makeText(context, "🔄 Synced: Active ride restored from backend", Toast.LENGTH_SHORT).show()
                }
                !hasBackendRide && currentRideIdFromState != null -> {
                    Toast.makeText(context, "🔄 Synced: Ride completed (cleared from backend)", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(currentLocation, mapViewInstance) {
        currentLocation?.let { location ->
            mapViewInstance?.let { mapView ->
                flyToLocation(mapView, location = location)
            }
        }
    }

    if (isOnline) {
        LaunchedEffect(Unit) {
            val locationTracker = LocationTracker(context)
            locationTracker.startLocationUpdates { location ->
                Timber.d("📍 LOCATION UPDATE: ${location.latitude}, ${location.longitude}")
                currentLocation = location.latitude to location.longitude
                viewModel.updateLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        }
    }

    LaunchedEffect(isInitialized) {
        if (!isInitialized) {
            coroutineScope.launch {
                isInitialized = true
                viewModel.initialize()
                val location = LocationTracker(context).getCurrentLocation()
                currentLocation = location
            }
        }
    }


//    if (isOnline && currentRideRequest == null) {
//        LaunchedEffect(rideRequest) {
//            Timber.d("🔄 RIDE STATE: New rideRequest received = ${rideRequest?.rideId}, Current = ${currentRideRequest?.rideId}")
//
//            if (rideRequest != null && currentRideRequest == null) {
//                currentRideRequest = rideRequest
//                showRidePopup = true
//                mapViewInstance?.let { mapView ->
//                    rideRequest?.pickupLocation?.let { location ->
//                        flyToLocation(mapView, latitude = location.latitude, longitude = location.longitude)
//                    }
//                }
//
//                val location = LocationTracker(context).getCurrentLocation()
//                viewModel.updateLocation(
//                    latitude = location!!.first,
//                    longitude = location.second
//                )
//            } else if (rideRequest == null) {
//                mapViewInstance?.let {
//                    clearPickupMarkerAndRouteLine()
//                }
//            } else {
//                Timber.d("🚫 Ignoring new ride request because a ride is already in progress.")
//            }
//        }
//    }

    LaunchedEffect(Unit) {
        val locationTracker = LocationTracker(context)
        locationTracker.startLocationUpdates { location ->
            // Only update UI location, periodic updates handle backend
            currentLocation = location.latitude to location.longitude
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
                    zoneId = zoneInfo!!.payload.zone.id,
                    rideTypeId = zoneInfo!!.payload.zone.rideTypeIds.first(),
                )
            } else {
                Toast.makeText(context, "Unable to get current location. Please check location permissions.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun toggleOnline() {
        if (isOnline) {
            currentRideRequest = null
            viewModel.goOffline()
        } else {
            goOnlineWithLocation()
        }
    }

    fun handleLogout() {
        coroutineScope.launch {
            viewModel.clearSession()
            viewModel.disconnect()
            onLogout()
        }
    }

    fun handleAcceptRide() {
        coroutineScope.launch {
            viewModel.acceptRide(currentRideRequest!!.rideId)
        }
    }

    fun handleDeclineRide() {
        coroutineScope.launch {
            viewModel.declineRide(currentRideRequest!!.rideId)
        }
    }

    LaunchedEffect(zoneInfo?.payload?.zone?.name) {
        val newZone = zoneInfo?.payload?.zone?.name
        if (newZone != null && newZone != lastZoneName) {
            lastZoneName = newZone
            Toast.makeText(context, "Zone Updated. Operational zone: $newZone", Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (selectedTab) {
            is BottomNavItem.Home -> {
                HomeTabContent(
                    context,
                    viewModel = viewModel,
                    showOnlineOfflineToggleButton = zoneInfo != null,
                    isOnline = isOnline,
                    onToggleOnline = { toggleOnline() },
                    onAcceptRide = { handleAcceptRide() },
                    onDeclineRide = { handleDeclineRide() },
                    currentRideRequest = currentRideRequest,
                    showRidePopup = showRidePopup,
                    onPopupDismiss = {
                        showRidePopup = false
                        currentRideRequest = null
                        clearPickupMarkerAndRouteLine()
                    },
                    setShowRidePopup = { showRidePopup = it },
                    setCurrentRideRequest = { currentRideRequest = it },
                    mapViewInstance = mapViewInstance,
                    setMapViewInstance = { mapViewInstance = it },
                    currentLocation = currentLocation,
                    showLoading = showLoading,
                    showLoadingText = showLoadingText,
                    rideAccepted = rideAccepted,
                    arrivedAtPickupPoint = arrivedAtPickupPoint,
                    rideStarted = rideStarted,
                    rideCompleted = rideCompleted
                )
            }

            is BottomNavItem.Activity -> ActivityScreen()
            is BottomNavItem.Account -> AccountScreen()
        }

        NavigationBar(
            containerColor = Color.White,
            contentColor = Color.Black,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            listOf(BottomNavItem.Home, BottomNavItem.Activity, BottomNavItem.Account).forEach { item ->
                NavigationBarItem(
                    selected = selectedTab == item,
                    onClick = { selectedTab = item },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selectedTab == item) Color.Black else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            color = if (selectedTab == item) Color.Black else Color.Gray
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color.Black,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}