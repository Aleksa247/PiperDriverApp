package com.piperrideshare.driver.api.models.response.websocket

data class RideModelChangedResponse(
    val rideId: String,
) : WebSocketResponse
