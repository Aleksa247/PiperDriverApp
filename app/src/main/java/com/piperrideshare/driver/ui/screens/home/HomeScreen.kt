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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.maps.MapView
import com.piperrideshare.driver.api.models.DriverAvailabilityState
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
    onNavigateToChat: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: WebSocketViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ==============================================
    // SINGLE SOURCE OF TRUTH - Backend Driver State
    // ==============================================

    val driverState by viewModel.driverState.collectAsState(initial = null)
    val currentAvailabilityState = driverState?.availabilityState ?: DriverAvailabilityState.OFFLINE
    val currentRideId = driverState?.currentRideId
    val stateRestored by viewModel.stateRestored.collectAsState()

    // ==============================================
    // OTHER WEBSOCKET DATA
    // ==============================================

    val rideRequest by viewModel.rideRequest.collectAsState()
    val rideModel by viewModel.rideModel.collectAsState()
    val riderInfo by viewModel.riderInfo.collectAsState()
    val zoneInfo by viewModel.zoneInfo.collectAsState()
    val showLoading by viewModel.showLoading.collectAsState()
    val showLoadingText by viewModel.showLoadingText.collectAsState()
    //styamamo - edit collect address states
    val rideRequestPickupAddress by viewModel.rideRequestPickupAddress.collectAsState(initial = null)
    val rideRequestDropoffAddress by viewModel.rideRequestDropoffAddress.collectAsState(initial = null)



    // ==============================================
    // LOCAL UI STATE
    // ==============================================

    var currentLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showRidePopup by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var currentRideRequest by remember { mutableStateOf<RideRequestedResponse?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var lastZoneName by remember { mutableStateOf<String?>(null) }
    
    // Persistent tab selection state
    var selectedTabLabel by rememberSaveable { mutableStateOf("Home") }
    val selectedTab = remember(selectedTabLabel) {
        when (selectedTabLabel) {
            "Activity" -> BottomNavItem.Activity
            "Account" -> BottomNavItem.Account
            else -> BottomNavItem.Home
        }
    }

    // ==============================================
    // INITIALIZATION
    // ==============================================

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

    // ==============================================
    // STATE RESTORATION FEEDBACK
    // ==============================================

    LaunchedEffect(stateRestored, currentAvailabilityState, currentRideId) {
        if (stateRestored) {
            val message = when {
                currentAvailabilityState == DriverAvailabilityState.ONLINE && currentRideId != null -> {
                    "🔄 State restored: You have an active ride."
                }
                currentAvailabilityState == DriverAvailabilityState.EN_ROUTE -> {
                    "🚗 State restored: En route to pickup."
                }
                currentAvailabilityState == DriverAvailabilityState.ARRIVED -> {
                    "📍 State restored: Arrived at pickup."
                }
                currentAvailabilityState == DriverAvailabilityState.IN_TRIP -> {
                    "🚕 State restored: Trip in progress."
                }
                currentAvailabilityState == DriverAvailabilityState.ONLINE -> {
                    "🟢 You're back online and ready for rides!"
                }
                else -> null
            }

            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    // ==============================================
    // ZONE INFO UPDATES
    // ==============================================

    LaunchedEffect(zoneInfo?.payload?.zone?.name) {
        val newZone = zoneInfo?.payload?.zone?.name
        if (newZone != null && newZone != lastZoneName) {
            lastZoneName = newZone
            Toast.makeText(context, "Zone Updated. Operational zone: $newZone", Toast.LENGTH_LONG).show()
        }
    }

    // ==============================================
    // LOCATION TRACKING
    // ==============================================

    LaunchedEffect(currentAvailabilityState) {
        if (currentAvailabilityState != DriverAvailabilityState.OFFLINE) {
            val locationTracker = LocationTracker(context)
            locationTracker.startLocationUpdates { location ->
                currentLocation = location.latitude to location.longitude
                viewModel.updateLocation(location.latitude, location.longitude)
            }
        }
    }

    // ==============================================
    // RIDE REQUEST HANDLING
    // ==============================================

    LaunchedEffect(rideRequest) {
        if (currentAvailabilityState == DriverAvailabilityState.ONLINE && rideRequest != null && currentRideRequest == null) {
            currentRideRequest = rideRequest
            showRidePopup = true

            // Fly to pickup location on map
            mapViewInstance?.let { mapView ->
                rideRequest?.pickupLocation?.let { location ->
                    flyToLocation(mapView, latitude = location.latitude, longitude = location.longitude)
                }
            }
        } else if (rideRequest == null) {
            mapViewInstance?.let {
                clearPickupMarkerAndRouteLine()
            }
        }
    }

    // ==============================================
    // LOCATION UPDATE FOR MAP
    // ==============================================

    LaunchedEffect(currentLocation, mapViewInstance) {
        currentLocation?.let { location ->
            mapViewInstance?.let { mapView ->
                flyToLocation(mapView, location = location)
            }
        }
    }

    // ==============================================
    // ACTION HANDLERS
    // ==============================================

    fun goOnlineWithLocation() {
        coroutineScope.launch {
            val location = LocationTracker(context).getCurrentLocation()
            if (location != null) {
                currentLocation = location
                zoneInfo?.let { zone ->
                    viewModel.goOnline(
                        latitude = location.first,
                        longitude = location.second,
                        zoneId = zone.payload.zone.id,
                        rideTypeId = zone.payload.zone.rideTypeIds.first(),
                    )
                }
            } else {
                Toast.makeText(context, "Unable to get current location. Please check location permissions.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun toggleOnline() {
        when (currentAvailabilityState) {
            DriverAvailabilityState.OFFLINE -> goOnlineWithLocation()
            DriverAvailabilityState.ONLINE -> viewModel.goOffline()
            else -> {
                Toast.makeText(context, "Cannot change online status during active ride", Toast.LENGTH_SHORT).show()
            }
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
        currentRideRequest?.let { request ->
            viewModel.acceptRide(request.rideId)
            showRidePopup = false
            currentRideRequest = null
        }
    }

    fun handleDeclineRide() {
        // For now, just clear the request (backend doesn't support decline yet)
        showRidePopup = false
        currentRideRequest = null
        clearPickupMarkerAndRouteLine()
    }

    // ==============================================
    // UI RENDERING
    // ==============================================

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(BottomNavItem.Home, BottomNavItem.Activity, BottomNavItem.Account).forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item,
                        onClick = { selectedTabLabel = item.label },
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
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                is BottomNavItem.Home -> {
                    HomeTabContent(
                        context = context,
                        viewModel = viewModel,
                        driverState = driverState,
                        currentAvailabilityState = currentAvailabilityState,
                        zoneInfo = zoneInfo,
                        rideRequest = currentRideRequest,
                        rideModel = rideModel,
                        riderInfo = riderInfo,
                        showRidePopup = showRidePopup,
                        showLoading = showLoading,
                        showLoadingText = showLoadingText,
                        currentLocation = currentLocation,
                        mapViewInstance = mapViewInstance,
                        onToggleOnline = ::toggleOnline,
                        onAcceptRide = ::handleAcceptRide,
                        onDeclineRide = ::handleDeclineRide,
                        onPopupDismiss = {
                            showRidePopup = false
                            currentRideRequest = null
                            clearPickupMarkerAndRouteLine()
                        },
                        setMapViewInstance = { mapViewInstance = it },
                        //styamamo - edit add parameters for addresses
                        rideRequestPickupAddress = rideRequestPickupAddress,
                        rideRequestDropoffAddress = rideRequestDropoffAddress,
                        onNavigateToChat = onNavigateToChat
                    )
                }
                is BottomNavItem.Activity -> ActivityScreen()
                is BottomNavItem.Account -> AccountScreen(
                    onLogout = ::handleLogout,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    }
}