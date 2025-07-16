package com.piperrideshare.driver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * RideRequestPopup - Displays incoming ride request details
 *
 * Shows a popup with ride information including pickup/dropoff locations,
 * estimated fare, and accept/decline buttons.
 *
 * @param rideRequest The ride request details
 * @param onAccept Callback when ride is accepted
 * @param onDecline Callback when ride is declined
 * @param onDismiss Callback when popup is dismissed
 *
 * @author Thomas Woodfin
 */
@Composable
fun RideRequestPopup(
    rideRequest: RideRequestedResponse,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit,
) {
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
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header
                Text(
                    text = "🚗 New Ride Request",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ride ID
                Text(
                    text = "Ride ID: ${rideRequest.rideId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pickup Location
                rideRequest.pickupLocation?.let { pickup ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "📍",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Column {
                            Text(
                                text = "Pickup",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = pickup.address ?: "${pickup.latitude}, ${pickup.longitude}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Dropoff Location
                rideRequest.dropoffLocation?.let { dropoff ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "🎯",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Column {
                            Text(
                                text = "Dropoff",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = dropoff.address ?: "${dropoff.latitude}, ${dropoff.longitude}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Ride Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Estimated Fare
                    rideRequest.estimatedFare?.let { fare ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "💰",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "$${String.format("%.2f", fare)}",
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

                    // Estimated Distance
                    rideRequest.estimatedDistance?.let { distance ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📏",
                                style = MaterialTheme.typography.bodyLarge,
                            )
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

                    // Estimated Duration
                    rideRequest.estimatedDuration?.let { duration ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⏱️",
                                style = MaterialTheme.typography.bodyLarge,
                            )
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

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PiperDriverButton(
                        text = "Decline",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            // Thomas Breakpoint: Set breakpoint here to debug ride decline button clicks
                            onDecline()
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White,
                            ),
                    )

                    PiperDriverButton(
                        text = "Accept",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            // Thomas Breakpoint: Set breakpoint here to debug ride accept button clicks
                            onAccept()
                        },
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
