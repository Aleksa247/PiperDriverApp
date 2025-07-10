package com.piperrideshare.driver.api.models.request

/**
 * Represents a ride request from a rider.
 */
data class RideRequest(
    val rideId: String,
    val riderId: String,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val pickupAddress: String,
    val dropoffLatitude: Double,
    val dropoffLongitude: Double,
    val dropoffAddress: String,
    val estimatedFare: Double,
    val estimatedDistance: Double,
    val estimatedDuration: Int // in minutes
)
