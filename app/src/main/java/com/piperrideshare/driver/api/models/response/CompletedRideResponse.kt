package com.piperrideshare.driver.api.models.response

/**
 * Represents a completed ride.
 */
data class CompletedRideResponse(
    val rideId: String,
    val riderId: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val startTime: Long,
    val endTime: Long,
    val distance: Double,
    val duration: Int, // in minutes
    val fare: Double,
    val tip: Double = 0.0,
    val rating: Double? = null,
)
