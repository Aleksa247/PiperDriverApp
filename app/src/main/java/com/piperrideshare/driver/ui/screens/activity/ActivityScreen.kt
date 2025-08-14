package com.piperrideshare.driver.ui.screens.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel

@Composable
fun ActivityScreen(viewModel: WebSocketViewModel = hiltViewModel()) {
    // Fetch ride history once when screen loads
    LaunchedEffect(Unit) {
        viewModel.goRideHistory()
    }

    val rideHistory by viewModel.rideHistory.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (rideHistory == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(rideHistory!!.rides) { ride ->
                    RideHistoryItem(ride)
                }
            }
        }
    }
}

@Composable
fun RideHistoryItem(ride: com.piperrideshare.driver.api.models.response.websocket.RideModelChangedResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Ride ID: ${ride.rideId}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Status: ${ride.status}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Fare: ${ride.estimatedFare / 100.0}", style = MaterialTheme.typography.bodyMedium)
            // Add more details as needed
        }
    }
}