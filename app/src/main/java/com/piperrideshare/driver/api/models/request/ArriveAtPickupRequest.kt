package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

data class ArriveAtPickupRequest(
    val rideId: String,
) : WebSocketRequest {
    override val type = "command"
    override val action = "arrive_at_pickup"

    override fun toJson() =
        JSONObject()
            .put("type", type)
            .put("action", action)
            .put("payload", JSONObject().put("ride_id", rideId))
}
