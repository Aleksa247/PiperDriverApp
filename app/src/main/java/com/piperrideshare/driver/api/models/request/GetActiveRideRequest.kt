package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

class GetActiveRideRequest : WebSocketRequest {
    override val type = "query"
    override val action = "get_active_ride"

    override fun toJson() =
        JSONObject()
            .put("type", type)
            .put("action", action)
            .put("payload", JSONObject())
}
