package com.piperrideshare.driver.api.models.request


import org.json.JSONObject


class GetEarningsRequest(
    private val requestId: String,
    private val timeFrame: String = "week"
) : WebSocketRequest {
    override val type = "query"
    override val action = "get_earnings"

    override fun toJson(): JSONObject {
        val payload = JSONObject()
            .put("time_frame", timeFrame)

        return JSONObject()
            .put("type", type)
            .put("action", action)
            .put("requestId", requestId)
            .put("payload", payload)
    }
}