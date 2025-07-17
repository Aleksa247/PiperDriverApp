package com.piperrideshare.driver.api.models.response.websocket

data class DriverModelChangedResponse(
    val driverId: String,
) : WebSocketResponse
