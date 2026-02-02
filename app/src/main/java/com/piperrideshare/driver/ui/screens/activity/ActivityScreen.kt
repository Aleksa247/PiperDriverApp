package com.piperrideshare.driver.ui.screens.activity

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.piperrideshare.driver.api.models.response.websocket.RideModelChangedResponse
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ActivityScreen(viewModel: WebSocketViewModel = hiltViewModel()) {
    // Fetch ride history once when screen loads
    LaunchedEffect(Unit) {
        viewModel.goRideHistory()
    }

    val rideHistory by viewModel.rideHistory.collectAsState()
    val rideHistoryValue = rideHistory
    
    // State to manage Drill-Down Navigation
    var selectedRide by remember { mutableStateOf<RideModelChangedResponse?>(null) }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        if (selectedRide != null) {
            // Detailed View
            RideDetailView(
                ride = selectedRide!!, 
                onBack = { selectedRide = null }
            )
        } else {
            // List View
            if (rideHistoryValue == null) {
                // Loading State or Initial State
                 Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            } else if (rideHistoryValue?.rides.isNullOrEmpty()) {
                // Empty State
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalTaxi, 
                            contentDescription = null, 
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No activity yet", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Activity",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(rideHistoryValue!!.rides!!) { ride ->
                        RideHistoryItem(ride = ride, onClick = { selectedRide = ride })
                    }
                }
            }
        }
    }
}

@Composable
fun RideHistoryItem(
    ride: RideModelChangedResponse,
    onClick: () -> Unit
) {
    val date = formatDateTime(ride.requestTime)
    // Calculate total: Actual Fare + Tip
    val fare = if (ride.actualFare > 0) ride.actualFare / 100.0 else ride.estimatedFare / 100.0
    val tip = ride.driverTip / 100.0
    val total = fare + tip
    
    val statusColor = getStatusColor(ride.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalTaxi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (ride.status == "COMPLETED") "Ride Completed" else ride.status.capitalize(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (tip > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Includes $${String.format("%.2f", tip)} tip",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50), // Green
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Price and Chevron
            Column(horizontalAlignment = Alignment.End) {
                 Text(
                    text = "$${String.format("%.2f", total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                 Text(
                    text = ride.status.capitalize(),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailView(
    ride: RideModelChangedResponse,
    onBack: () -> Unit
) {
    // Handle system back press
    BackHandler(onBack = onBack)

    val scrollState = rememberScrollState()
    
    val fare = if (ride.actualFare > 0) ride.actualFare / 100.0 else ride.estimatedFare / 100.0
    val tip = ride.driverTip / 100.0
    val total = fare + tip
    
    val date = formatDateTime(ride.requestTime)
    
    val statusColor = getStatusColor(ride.status)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ride Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (tip > 0) {
                        Text(
                            text = "(Includes $${String.format("%.2f", tip)} tip)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                     Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(statusColor.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = ride.status,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Route Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                 elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Trip Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Pickup
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.Green, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Pickup", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = ride.pickupAddress ?: "Lat: ${String.format("%.4f", ride.pickupLocation?.latitude ?: 0.0)}, Lng: ${String.format("%.4f", ride.pickupLocation?.longitude ?: 0.0)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                             ride.requestTime?.let {
                                Text(formatTime(it), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(start = 32.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dropoff
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Dropoff", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = ride.dropoffAddress ?: "Lat: ${String.format("%.4f", ride.dropoffLocation?.latitude ?: 0.0)}, Lng: ${String.format("%.4f", ride.dropoffLocation?.longitude ?: 0.0)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                             ride.endTime?.let {
                                Text(formatTime(it), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Fare Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                 elevation = CardDefaults.cardElevation(2.dp)
            ) {
                 Column(modifier = Modifier.padding(16.dp)) {
                    Text("Fare Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    FareRow("Base Fare", ride.estimatedFare / 100.0)
                    if (ride.driverTip > 0) FareRow("Tip", ride.driverTip / 100.0)
                    if (ride.cancellationFee > 0) FareRow("Cancellation Fee", ride.cancellationFee / 100.0)
                    if (ride.waitTimeFee > 0) FareRow("Wait Time Fee", ride.waitTimeFee / 100.0)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text("$${String.format("%.2f", total)}", fontWeight = FontWeight.Bold)
                    }
                 }
            }
            
            // Rider & Rating info (if available)
            if (ride.rating > 0 || ride.riderId.isNotEmpty()) {
                 Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rider Info", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rider ID: ${ride.riderId}", style = MaterialTheme.typography.bodyMedium)
                        
                        if (ride.rating > 0) {
                             Spacer(modifier = Modifier.height(8.dp))
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Rating: ${ride.rating}", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            }
                            if (ride.feedback.isNotEmpty()) {
                                Text("Feedback: \"${ride.feedback}\"", style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FareRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Black.copy(alpha = 0.7f))
        Text("$${String.format("%.2f", amount)}", style = MaterialTheme.typography.bodyMedium)
    }
}

// Helper functions for Date/Time formatting
fun formatDateTime(isoString: String?): String {
    if (isoString.isNullOrEmpty() || isoString.startsWith("0001")) return "N/A"
    return try {
        val instant = Instant.parse(isoString)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • h:mm a")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        "Invalid Date"
    }
}

fun formatTime(isoString: String?): String {
    if (isoString.isNullOrEmpty() || isoString.startsWith("0001")) return ""
    return try {
        val instant = Instant.parse(isoString)
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        ""
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "COMPLETED" -> Color(0xFF4CAF50) // Green
        "CANCELLED" -> Color(0xFFE53935) // Red
        "IN_PROGRESS" -> Color(0xFF2196F3) // Blue
        else -> Color.Gray
    }
}

private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}