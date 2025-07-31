package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

class GetRideHistoryRequest(
    val requestId: String,
) : WebSocketRequest {
    override val type = "query"
    override val action = "get_ride_history"

    override fun toJson(): JSONObject =
        JSONObject()
            .put("type", type)
            .put("action", action)
            .put("requestId", requestId)
            .put("payload", JSONObject())
}
