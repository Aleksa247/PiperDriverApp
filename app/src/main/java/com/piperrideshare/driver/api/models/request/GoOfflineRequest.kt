package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

class GoOfflineRequest : WebSocketRequest {
    override val type = "command"
    override val action = "go_offline"

    override fun toJson(): JSONObject =
        JSONObject()
            .put("type", type)
            .put("action", action)
            .put("payload", JSONObject())
}
