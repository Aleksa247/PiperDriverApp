package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.Gson
import org.json.JSONObject
import timber.log.Timber

/**
 * WebSocketResponseParser - Parses incoming WebSocket messages
 *
 * Handles different types of WebSocket responses including:
 * - Ride requests
 * - Model updates
 * - Zone information
 * - Notifications
 * - Action responses (go_online, etc.)
 *
 * @author Thomas Woodfin
 */
object WebSocketResponseParser {
    private val gson = Gson()

    fun parse(json: JSONObject): WebSocketResponse {
        val type = json.optString("type")
        val action = json.optString("action")
        val payload = json.optJSONObject("payload")
        val status = json.optString("status")
        val error = json.optString("error")

        return when (type) {
            "notification" ->
                when (action) {
                    "ride_requested" -> {
                        Timber.d("🚗 WEBSOCKET: Parsing incoming ride request")
                        try {
                            if (payload != null) {
                                // Parse only the payload JSON object into RideRequestedResponse
                                gson.fromJson(payload.toString(), RideRequestedResponse::class.java)
                            } else {
                                Timber.e("❌ ride_requested payload is null")
                                UnknownResponse(json.toString())
                            }
                        } catch (e: Exception) {
                            Timber.e("❌ RIDE REQUEST PARSE ERROR: ${e.message}")
                            val rideId = payload?.optString("rideId")
                            if (rideId != null) {
                                RideRequestedResponse(rideId)
                            } else {
                                UnknownResponse(json.toString())
                            }
                        }
                    }

                    "zone_info" -> {
                        Timber.d("📍 WEBSOCKET: Parsing zone info")
                        try {
                            gson.fromJson(json.toString(), ZoneInfoResponse::class.java)
                        } catch (e: Exception) {
                            Timber.e(" ZONE INFO PARSE ERROR: ${e.message}")
                            UnknownResponse(json.toString())
                        }
                    }

                    else -> UnknownResponse(json.toString())
                }

            "model_update" ->
                when (action) {
                    "driver_model_changed" -> {
                        Timber.d("👤 WEBSOCKET: Driver model changed")
                        try {
                            if (payload != null) {
                                // Parse the payload directly into DriverModelChangedResponse
                                val response = gson.fromJson(payload.toString(), DriverModelChangedResponse::class.java)
                                Timber.d("WEBSOCKET: Successfully parsed driver model - ID: ${response.driverId}, State: ${response.availabilityState}")
                                response
                            } else {
                                Timber.e("MISSING payload in driver_model_changed")
                                UnknownResponse(json.toString())
                            }
                        } catch (e: Exception) {
                            Timber.e("DRIVER MODEL PARSE ERROR: ${e.message}")
                            Timber.e("Raw payload: ${payload?.toString()}")
                            UnknownResponse(json.toString())
                        }
                    }

                    "ride_model_changed" -> {
                        Timber.d("🚕 WEBSOCKET: Ride model changed")

                        return try {
                            val response = gson.fromJson(payload!!.toString(), RideModelChangedResponse::class.java)
                            response
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Failed to parse RideModelChangedResponse")
                            UnknownResponse(json.toString())
                        }
                    }

                    else -> UnknownResponse(json.toString())
                }

            "response" ->
                when (action) {
                    "go_online", "update_location", "accept_ride",
                    "arrive_at_pickup", "start_ride", "complete_ride",
                    -> {
                        Timber.d("✅ WEBSOCKET: $action response - Status: $status")
                        ActionResponse(type, action, status, error, payload)
                    }

                    else -> UnknownResponse(json.toString())
                }

            else -> UnknownResponse(json.toString())
        }
    }
}
