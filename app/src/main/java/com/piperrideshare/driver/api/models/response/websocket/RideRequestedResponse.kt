package com.piperrideshare.driver.api.models.response.websocket

data class RideRequestedResponse(
    val rideId: String,
) : WebSocketResponse
