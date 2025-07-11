package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

data class UpdateLocationRequest(
    val latitude: Double,
    val longitude: Double,
) : WebSocketRequest {
    override val type = "command"
    override val action = "update_location"

    override fun toJson(): JSONObject {
        val location =
            JSONObject()
                .put("latitude", latitude)
                .put("longitude", longitude)

        return JSONObject()
            .put("type", type)
            .put("action", action)
            .put("payload", JSONObject().put("location", location))
    }
}
