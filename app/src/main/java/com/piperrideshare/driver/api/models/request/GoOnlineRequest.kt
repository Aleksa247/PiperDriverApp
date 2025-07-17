package com.piperrideshare.driver.api.models.request

import org.json.JSONObject

data class GoOnlineRequest(
    val latitude: Double,
    val longitude: Double,
    val deviceId: String,
    val zoneId: String,
    val rideTypeId: String,
) : WebSocketRequest {
    override val type = "command"
    override val action = "go_online"

    override fun toJson() =
        JSONObject().apply {
            put("type", type)
            put("action", action)
            put(
                "payload",
                JSONObject().apply {
                    put(
                        "location",
                        JSONObject().apply {
                            put("latitude", latitude)
                            put("longitude", longitude)
                        },
                    )
                    put("device_id", deviceId)
                    put("operational_zone_id", zoneId)
                    put("ride_type_id", rideTypeId)
                },
            )
        }
}
