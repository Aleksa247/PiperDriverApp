package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

interface WebSocketRequest {
    val type: String
    val action: String

    fun toJson(): JSONObject
}
