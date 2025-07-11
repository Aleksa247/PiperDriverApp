package com.piperrideshare.driver.ui.screens.home

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.piperrideshare.driver.ui.components.PiperDriverButton
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel
import com.piperrideshare.driver.utils.LocationTracker
import com.piperrideshare.driver.utils.PermissionHandler
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToRideDetail: (String) -> Unit,
    viewModel: WebSocketViewModel = hiltViewModel(),
) {
    var isOnline by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var pendingOnlineRequest by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val rideRequest by viewModel.rideRequest.collectAsState()
    val driverModel by viewModel.driverModel.collectAsState()

    fun goOnlineWithLocation() {
        coroutineScope.launch {
            val locationTracker = LocationTracker(context)
            val location = locationTracker.getCurrentLocation()

            if (location != null) {
                currentLocation = location
                viewModel.goOnline(
                    latitude = location.first,
                    longitude = location.second,
                    deviceId = "device123",
                    zoneId = "zone456",
                    rideTypeId = "standard",
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
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Driver App",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        driverModel?.driverId?.let { driverId ->
            Text("Driver ID: $driverId", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        PiperDriverButton(
            text = if (isOnline) "Go Offline" else "Go Online",
            modifier = Modifier.fillMaxWidth(),
            onClick = { toggleOnline() },
        )

        Spacer(modifier = Modifier.height(16.dp))

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

                    rideRequest?.let { request ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("🚗 New Ride ID: ${request.rideId}", style = MaterialTheme.typography.bodyLarge)
                        PiperDriverButton(
                            text = "View Ride Details",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToRideDetail(request.rideId) },
                        )
                    }
                }
            }
        }
    }
}
