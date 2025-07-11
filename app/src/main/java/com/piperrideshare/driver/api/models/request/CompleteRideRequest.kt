package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

data class CompleteRideRequest(
    val rideId: String,
    val distance: Double,
) : WebSocketRequest {
    override val type = "command"
    override val action = "complete_ride"

    override fun toJson(): JSONObject {
        val payload =
            JSONObject()
                .put("ride_id", rideId)
                .put("distance", distance)

        return JSONObject()
            .put("type", type)
            .put("action", action)
            .put("payload", payload)
    }
}
