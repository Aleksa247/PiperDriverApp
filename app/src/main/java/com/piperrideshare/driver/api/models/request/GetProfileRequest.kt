package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

class GetProfileRequest(
    private val requestId: String
) : WebSocketRequest {
    override val type = "query"
    override val action = "get_profile"

    override fun toJson(): JSONObject =
        JSONObject()
            .put("type", type)
            .put("action", action)
            .put("requestId", requestId)
            .put("payload", JSONObject())
}