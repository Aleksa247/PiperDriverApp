package com.piperrideshare.driver.api.models.response.websocket

import org.json.JSONObject

object WebSocketResponseParser {
    fun parse(json: JSONObject): WebSocketResponse {
        val type = json.optString("type")
        val action = json.optString("action")
        val payload = json.optJSONObject("payload")

        return when (type) {
            "notification" ->
                when (action) {
                    "ride_requested" -> {
                        val rideId = payload?.optString("ride_id")
                        if (rideId != null) RideRequestedResponse(rideId) else UnknownResponse(json.toString())
                    }
                    else -> UnknownResponse(json.toString())
                }

            "model_update" ->
                when (action) {
                    "driver_model_changed" -> {
                        val id = payload?.optString("id")
                        if (id != null) DriverModelChangedResponse(id) else UnknownResponse(json.toString())
                    }
                    "ride_model_changed" -> {
                        val id = payload?.optString("id")
                        if (id != null) RideModelChangedResponse(id) else UnknownResponse(json.toString())
                    }
                    else -> UnknownResponse(json.toString())
                }

            else -> UnknownResponse(json.toString())
        }
    }
}
