package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

data class StartRideRequest(
    val rideId: String,
) : WebSocketRequest {
    override val type = "command"
    override val action = "start_ride"

    override fun toJson() =
        JSONObject()
            .put("type", type)
            .put("action", action)
            .put("payload", JSONObject().put("ride_id", rideId))
}
