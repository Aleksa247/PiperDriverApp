package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.annotations.SerializedName

/**
 * RideRequestedResponse - WebSocket response for new ride requests
 *
 * Contains detailed information about a new ride request including
 * pickup/dropoff locations, estimated fare, and rider details.
 */
data class RideRequestedResponse(
    @SerializedName("rideId")
    val rideId: String = "",
    @SerializedName("startingLocation")
    val pickupLocation: Location? = null,
    @SerializedName("endingLocation")
    val dropoffLocation: Location? = null,
    @SerializedName("totalEarnings")
    val estimatedFare: Double? = null,
    @SerializedName("estimatedLength")
    val estimatedDistance: Double? = null,
    @SerializedName("estimatedTime")
    val estimatedDuration: Int? = null,
) : WebSocketResponse()

data class Location(
    val latitude: Double,
    val longitude: Double,
)
