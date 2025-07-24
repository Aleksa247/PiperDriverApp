package com.piperrideshare.driver.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.piperrideshare.driver.api.models.response.websocket.RideRequestedResponse

@SuppressLint("DefaultLocale")
@Composable
fun RideRequestPopup(
    rideRequest: RideRequestedResponse,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .heightIn(min = 200.dp, max = 500.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(24.dp)
                            .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Header
                    Text(
                        text = "🚗 New Ride Request",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ride ID: ${rideRequest.rideId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    rideRequest.pickupLocation?.let { pickup ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("📍", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.size(8.dp))
                            Column {
                                Text(
                                    text = "Pickup",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "${pickup.latitude}, ${pickup.longitude}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    rideRequest.dropoffLocation?.let { dropoff ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("🎯", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.size(8.dp))
                            Column {
                                Text(
                                    text = "Dropoff",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "${dropoff.latitude}, ${dropoff.longitude}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        rideRequest.estimatedFare?.let { fare ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💰", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "$${String.format("%.2f", fare/100)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Est. Fare",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        rideRequest.estimatedDistance?.let { distance ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📏", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "${String.format("%.1f", distance)} mi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Distance",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        rideRequest.estimatedDuration?.let { duration ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⏱️", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "$duration min",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Duration",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PiperDriverButton(
                            text = "Decline",
                            modifier = Modifier.weight(1f),
                            onClick = onDecline,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White,
                                ),
                        )

                        PiperDriverButton(
                            text = "Accept",
                            modifier = Modifier.weight(1f),
                            onClick = onAccept,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color.Green,
                                    contentColor = Color.White,
                                ),
                        )
                    }
                }
            }
        }
    }
}
