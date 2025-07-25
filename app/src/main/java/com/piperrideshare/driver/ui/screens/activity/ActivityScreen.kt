package com.piperrideshare.driver.ui.screens.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel
import kotlinx.coroutines.delay

@Composable
fun ActivityScreen(viewModel: WebSocketViewModel = hiltViewModel()) {
//    val rideHistory by remember { derivedStateOf { viewModel.rideHistory } }

    // Fetch ride history once when screen loads
    LaunchedEffect(Unit) {
        delay(500) // Optional: delay to wait for socket connection
        viewModel.goRideHistory()
    }

    Box(modifier = Modifier.fillMaxSize()) {
//        if (rideHistory.isEmpty()) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Text("No rides found", style = MaterialTheme.typography.bodyLarge)
//            }
//        } else {
//            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//                items(rideHistory) { ride ->
//                    RideHistoryItem(ride)
//                }
//            }
//        }
    }
}
