package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

data class GetRiderInfoRequest(
    val rideId: String,
    val riderId: String // This property already exists in your data class
) : WebSocketRequest {
    override val type = "query"
    override val action = "get_rider_info"

    override fun toJson() =
        JSONObject()
            .put("type", type)
            .put("action", action)
            .put(
                "payload",
                JSONObject()
                    .put("ride_id", rideId)
                    .put("rider_id", riderId) // <<< Add this line
            )
}