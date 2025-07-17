package com.piperrideshare.driver.api.models

import com.piperrideshare.driver.api.models.request.RideRequest

/**
 * Represents the current state of the driver.
 */
data class DriverState(
    val isOnline: Boolean = false,
    val currentLocation: Pair<Double, Double>? = null,
    val currentRideId: String? = null,
    val currentRideRequest: RideRequest? = null,
    val availableBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val rating: Double = 0.0,
)
