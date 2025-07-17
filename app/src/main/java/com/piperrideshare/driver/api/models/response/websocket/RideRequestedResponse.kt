package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.annotations.SerializedName

/**
 * RideRequestedResponse - WebSocket response for new ride requests
 *
 * Contains detailed information about a new ride request including
 * pickup/dropoff locations, estimated fare, and rider details.
 *
 * @author Thomas Woodfin
 */
data class RideRequestedResponse(
    val rideId: String,
    val pickupLocation: Location? = null,
    val dropoffLocation: Location? = null,
    val estimatedFare: Double? = null,
    val estimatedDistance: Double? = null,
    val estimatedDuration: Int? = null, // in minutes
    val riderName: String? = null,
    val rideType: String? = null,
    @SerializedName("request_time")
    val requestTime: String? = null,
) : WebSocketResponse()

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
)
