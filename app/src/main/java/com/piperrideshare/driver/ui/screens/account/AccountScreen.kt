package com.piperrideshare.driver.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.piperrideshare.driver.api.models.response.websocket.EarningsResponse
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel

@Composable
fun AccountScreen(
    viewModel: WebSocketViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // State from ViewModel
    val driverState by viewModel.driverState.collectAsState(initial = null)
    val profileResponse by viewModel.profileResponse.collectAsState()
    val earningsResponse by viewModel.earningsResponse.collectAsState()
    var selectedTimeFrame by remember { mutableStateOf("week") }

    // Load profile and earnings data when screen opens
    LaunchedEffect(Unit) {
        viewModel.getProfile()
        viewModel.getEarnings(selectedTimeFrame)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = {
                    viewModel.getProfile()
                    viewModel.getEarnings(selectedTimeFrame)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Profile"
                )
            }
        }

        // Profile Card
        ProfileCard(
            driverState = driverState,
            profilePayload = profileResponse?.payload
        )

        // Earnings Card
        EarningsCard(
            earningsResponse = earningsResponse,
            selectedTimeFrame = selectedTimeFrame,
            onTimeFrameChanged = { timeFrame ->
                selectedTimeFrame = timeFrame
                viewModel.getEarnings(timeFrame)
            }
        )

        // Driver Status Card
        DriverStatusCard(driverState = driverState)
    }
}

@Composable
private fun ProfileCard(
    driverState: com.piperrideshare.driver.api.models.DriverState?,
    profilePayload: com.piperrideshare.driver.api.models.response.websocket.ProfilePayload?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profile Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (profilePayload == null && driverState == null) {
                // Loading state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Text("Loading profile...")
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Photo
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profilePayload?.profilePhotoUrl ?: driverState?.profilePhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        fallback = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person)
                    )

                    // Name and Details
                    Column {
                        Text(
                            text = "${profilePayload?.firstName ?: driverState?.firstName} ${profilePayload?.lastName ?: driverState?.lastName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = profilePayload?.email ?: driverState?.email ?: "No email",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = profilePayload?.phone ?: driverState?.phone ?: "No phone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Rating and Rides
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Text(
                                text = "${profilePayload?.ratingAverage ?: driverState?.ratingAverage ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "• ${profilePayload?.totalRides ?: driverState?.totalRides ?: 0} rides",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (profilePayload?.isActive == true || driverState?.isActive == true) {
                        StatusBadge("Active", Color(0xFF4CAF50))
                    }
                    if (profilePayload?.isVerified == true || driverState?.isVerified == true) {
                        StatusBadge("Verified", Color(0xFF2196F3))
                    }
                }
            }
        }
    }
}

@Composable
private fun EarningsCard(
    earningsResponse: EarningsResponse?,
    selectedTimeFrame: String,
    onTimeFrameChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Earnings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (earningsResponse == null) {
                // Loading state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Text("Loading earnings...")
                }
            } else {
                // Time frame selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("week", "month", "all").forEach { timeFrame ->
                        StatusBadge(
                            text = timeFrame.replaceFirstChar { it.uppercase() },
                            color = if (selectedTimeFrame == timeFrame)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Gray,
                            onClick = { onTimeFrameChanged(timeFrame) }
                        )
                    }
                }

                // Earnings summary
                if (selectedTimeFrame == "week" || selectedTimeFrame == "month") { // Weekly/Monthly
                    EarningsItem(
                        label = "Total Earnings",
                        value = "$${String.format("%.2f", (earningsResponse.earnings ?: 0) / 100.0)}"
                    )
                } else { // All time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EarningsItem(
                            label = "Total Earnings",
                            value = "$${String.format("%.2f", (earningsResponse.totalEarnings ?: 0) / 100.0)}"
                        )
                        EarningsItem(
                            label = "Available",
                            value = "$${String.format("%.2f", (earningsResponse.availableBalance ?: 0) / 100.0)}"
                        )
                        EarningsItem(
                            label = "Pending",
                            value = "$${String.format("%.2f", (earningsResponse.pendingBalance ?: 0) / 100.0)}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverStatusCard(
    driverState: com.piperrideshare.driver.api.models.DriverState?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Driver Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (driverState != null) {
                Text(
                    text = "Current Status: ${driverState.getStateDescription()}",
                    style = MaterialTheme.typography.bodyMedium
                )

                driverState.currentRideId?.let { rideId ->
                    Text(
                        text = "Active Ride: $rideId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                //styamamo - edit display address
                val locationText = when {
                    driverState.currentAddress != null -> driverState.currentAddress
                    driverState.currentLocation != null -> {
                        val (lat, lng) = driverState.currentLocation
                        "Location: ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}"
                    }

                    else -> "No location available"
                }
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("Loading driver status...")
            }
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = { onClick?.invoke() },
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EarningsItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}