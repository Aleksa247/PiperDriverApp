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
        val requestId = json.optString("requestId")
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


            "chat_message" ->
                when (action) {
                    "new_message" -> {
                        Timber.d("💬 WEBSOCKET: New chat message received")
                        try {
                            // The payload comes wrapped in a WebSocketResponse structure in iOS
                            // "payload": { "type": "response", "payload": { "id": "...", "message": "..." } }
                            // BUT accessing raw "payload" here (line 25) gets the inner object directly if structure is flat
                            // Let's assume standard structure: payload is the ChatMessage object
                            
                            // iOS Decode logic:
                            // let decoded = try decoder.decode(WebSocketResponse<ChatMessagePayload>.self, from: data)
                            // This implies the message structure is:
                            // { "type": "chat_message", "action": "new_message", "payload": { "message": "...", ... } }
                            
                            if (payload != null) {
                                val message = gson.fromJson(payload.toString(), ChatMessage::class.java)
                                NewMessageResponse(message)
                            } else {
                                Timber.e("❌ new_message payload is null")
                                UnknownResponse(json.toString())
                            }
                        } catch (e: Exception) {
                            Timber.e("❌ CHAT MESSAGE PARSE ERROR: ${e.message}")
                            UnknownResponse(json.toString())
                        }
                    }
                    else -> UnknownResponse(json.toString())
                }

            "response" ->
                when (action) {
                    "go_online", "update_location", "accept_ride",
                    "arrive_at_pickup", "start_ride", "complete_ride",
                    "send_message" // Added send_message here as a generic action response
                    -> {
                        Timber.d("✅ WEBSOCKET: $action response - Status: $status")
                        ActionResponse(type, action, status, error, payload)
                    }

                    "get_rider_info" -> {
                        Timber.d("👤 WEBSOCKET: Rider info response")
                        try {
                            if (payload != null) {
                                gson.fromJson(payload.toString(), RiderInfoResponse::class.java)
                            } else {
                                Timber.e("❌ get_rider_info payload is null")
                                UnknownResponse(json.toString())
                            }
                        } catch (e: Exception) {
                            Timber.e("❌ RIDER INFO PARSE ERROR: ${e.message}")
                            UnknownResponse(json.toString())
                        }
                    }

                    "get_profile" -> {
                        Timber.d("📋 WEBSOCKET: Profile response")
                        try {
                            if (payload != null) {
                                val response = gson.fromJson(payload.toString(), ProfileResponse::class.java)
                                // Add requestId if it was in the parent JSON
                                response.copy(requestId = requestId.takeIf { it.isNotBlank() })
                            } else {
                                Timber.e("❌ get_profile payload is null")
                                UnknownResponse(json.toString())
                            }
                        } catch (e: Exception) {
                            Timber.e("❌ PROFILE PARSE ERROR: ${e.message}")
                            UnknownResponse(json.toString())
                        }
                    }

                    "get_earnings" -> {
                        Timber.d("💰 WEBSOCKET: Earnings response")
                        try {
                            if (payload != null) {
                                val response = gson.fromJson(payload.toString(), EarningsResponse::class.java)
                                // Add requestId if it was in the parent JSON
                                response.copy(requestId = requestId.takeIf { it.isNotBlank() })
                            } else {
                                Timber.e("❌ get_earnings payload is null")
                                UnknownResponse(json.toString())
                            }
                        } catch (e: Exception) {
                            Timber.e("❌ EARNINGS PARSE ERROR: ${e.message}")
                            UnknownResponse(json.toString())
                        }
                    }

                    "get_ride_history" -> {
                        Timber.d("📜 WEBSOCKET: Ride history response")
                        try {
                            gson.fromJson(json.toString(), RideHistoryResponse::class.java)
                        } catch (e: Exception) {
                            Timber.e("❌ RIDE HISTORY PARSE ERROR: ${e.message}")
                            UnknownResponse(json.toString())
                        }
                    }
                    
                    "get_chat_history" -> {
                        Timber.d("💬 WEBSOCKET: Chat history response")
                         try {
                             // Payload is a list of messages: [ {msg1}, {msg2} ]
                             // But 'payload' variable is a JSONObject? from line 25.
                             // if payload is a JSONArray, json.optJSONObject("payload") returns null!
                             
                             // We need to re-fetch payload as generic or check if it's array
                             val payloadArray = json.optJSONArray("payload")
                             if (payloadArray != null) {
                                 val messages = ArrayList<ChatMessage>()
                                 for (i in 0 until payloadArray.length()) {
                                     val msgJson = payloadArray.getJSONObject(i)
                                     messages.add(gson.fromJson(msgJson.toString(), ChatMessage::class.java))
                                 }
                                 ChatHistoryResponse(messages)
                             } else {
                                 // Empty list or null
                                 ChatHistoryResponse(emptyList())
                             }
                        } catch (e: Exception) {
                            Timber.e("❌ CHAT HISTORY PARSE ERROR: ${e.message}")
                            UnknownResponse(json.toString())
                        }
                    }

                    else -> UnknownResponse(json.toString())
                }

            else -> UnknownResponse(json.toString())
        }
    }
}
