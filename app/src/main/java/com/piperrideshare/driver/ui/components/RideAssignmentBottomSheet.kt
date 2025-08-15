package com.piperrideshare.driver.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.piperrideshare.driver.api.models.response.websocket.LatLng
import com.piperrideshare.driver.api.models.response.websocket.RideModelChangedResponse
import com.piperrideshare.driver.api.models.response.websocket.RiderInfoResponse
import com.piperrideshare.driver.utils.openGoogleMaps

@Composable
fun RideAssignmentBottomSheet(
    rideModel: RideModelChangedResponse?,
    riderInfo: RiderInfoResponse?,
    currentRideStatus: String?,
    onArriveAtPickup: () -> Unit,
    onStartRide: () -> Unit,
    onCompleteRide: () -> Unit,
    onCallRider: () -> Unit,
) {
    if (rideModel == null) return
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with ride status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🚗 Active Ride",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            StatusChip(status = currentRideStatus ?: "unknown")
        }

        // Rider Information Section
        riderInfo?.let { rider ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Rider Details
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = rider.name ?: "Unknown Rider",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Call Button
                    IconButton(
                        onClick = onCallRider,
                        modifier = Modifier
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Rider",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        } ?: run {
            // Loading state for rider info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Loading rider information...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Trip Details Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Trip Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Pickup Location
                LocationRow(
                    icon = "📍",
                    label = "Pickup",
                    location = rideModel.pickupLocation,
                    isCurrentLocation = currentRideStatus == "accepted",
                    onNavigate = {
                        rideModel.pickupLocation?.let {
                            openGoogleMaps(context, it.latitude, it.longitude)
                        }
                    }
                )

                // Dropoff Location
                LocationRow(
                    icon = "🎯",
                    label = "Dropoff",
                    location = rideModel.dropoffLocation,
                    isCurrentLocation = false,
                    onNavigate = {
                        rideModel.dropoffLocation?.let {
                            openGoogleMaps(context, it.latitude, it.longitude)
                        }
                    }
                )

                // Trip Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        icon = "💰",
                        value = "${String.format("%.2f", rideModel.driverEarning.toDouble() / 100)}",
                        label = "Your Earning"
                    )
                    StatItem(
                        icon = "📏",
                        value = "${String.format("%.1f", rideModel.distance.toDouble() / 1000)} mi",
                        label = "Distance"
                    )
                    StatItem(
                        icon = "⏱️",
                        value = "${rideModel.duration} min",
                        label = "Duration"
                    )
                }
            }
        }

        // Action Button
        ActionButton(
            status = currentRideStatus,
            onArriveAtPickup = onArriveAtPickup,
            onStartRide = onStartRide,
            onCompleteRide = onCompleteRide
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (color, text) = when (status) {
        "accepted" -> Color(0xFF2196F3) to "En Route to Pickup"
        "driver_arrived" -> Color(0xFF9C27B0) to "Arrived at Pickup"
        "in_progress" -> Color(0xFF4CAF50) to "In Progress"
        else -> Color.Gray to "Unknown"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun LocationRow(
    icon: String,
    label: String,
    location: LatLng?,
    isCurrentLocation: Boolean,
    onNavigate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isCurrentLocation) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• Navigate Here",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = location?.let { "${it.latitude}, ${it.longitude}" } ?: "Unknown location",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onNavigate) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Navigate"
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButton(
    status: String?,
    onArriveAtPickup: () -> Unit,
    onStartRide: () -> Unit,
    onCompleteRide: () -> Unit
) {
    val (buttonText, buttonColor, action) = when (status) {
        "accepted" -> Triple("Arrive at Pickup", Color(0xFF2196F3), onArriveAtPickup)
        "driver_arrived" -> Triple("Start Ride", Color(0xFF4CAF50), onStartRide)
        "in_progress" -> Triple("Complete Ride", Color(0xFFFF9800), onCompleteRide)
        else -> Triple("Unknown Action", Color.Gray) { }
    }

    PiperDriverButton(
        text = buttonText,
        onClick = action,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White
        )
    )
}