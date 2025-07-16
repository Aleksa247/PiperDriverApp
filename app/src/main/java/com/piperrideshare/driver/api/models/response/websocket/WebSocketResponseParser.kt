package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.Gson
import org.json.JSONObject

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

    // Thomas Breakpoint: Set breakpoint here to see all incoming WebSocket messages
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
                        // T@Thomas - BREAKPOINT HERE: Incoming ride request WebSocket notification
                        // This is where ride requests are parsed from WebSocket messages
                        println("🚗 WEBSOCKET PARSER: Parsing incoming ride request notification")
                        try {
                            val rideRequestResponse = gson.fromJson(json.toString(), RideRequestedResponse::class.java)
                            println("✅ RIDE REQUEST PARSED: ID=${rideRequestResponse.rideId}, Fare=${rideRequestResponse.estimatedFare}")
                            RideRequestedResponse(
                                rideId = rideRequestResponse.rideId,
                                pickupLocation = rideRequestResponse.pickupLocation,
                                dropoffLocation = rideRequestResponse.dropoffLocation,
                                estimatedFare = rideRequestResponse.estimatedFare,
                                estimatedDistance = rideRequestResponse.estimatedDistance,
                                estimatedDuration = rideRequestResponse.estimatedDuration,
                                riderName = rideRequestResponse.riderName,
                                rideType = rideRequestResponse.rideType,
                                requestTime = rideRequestResponse.requestTime,
                            )
                        } catch (e: Exception) {
                            println("❌ RIDE REQUEST PARSE ERROR: ${e.message}")
                            // Fallback to simple ride ID parsing
                            val rideId = payload?.optString("ride_id")
                            if (rideId != null) RideRequestedResponse(rideId) else UnknownResponse(json.toString())
                        }
                    }
                    // Thomas Breakpoint: Set breakpoint here to debug zone info parsing
                    "zone_info" -> {
                        //                                                                          @Thomas - BREAKPOINT HERE: Zone info notification received
                        // Parse zone information from WebSocket payload
                        try {
                            val zoneInfoResponse = gson.fromJson(json.toString(), ZoneInfoResponse::class.java)
                            ZoneInfoResponse(zoneInfoResponse.type, zoneInfoResponse.action, zoneInfoResponse.payload)
                        } catch (e: Exception) {
                            println("❌ ZONE INFO PARSE ERROR: ${e.message}")
                            UnknownResponse(json.toString())
                        }
                    }
                    // Thomas Breakpoint: Set breakpoint here if getting unknown notification types
                    else -> UnknownResponse(json.toString())
                }

            "model_update" ->
                when (action) {
                    "driver_model_changed" -> {
                        // Thomas Breakpoint: TEMPORARY - Also create fake ride request for testing popup
                        // @Thomas - Remove this when backend sends proper ride_requested notifications
                        println("🚨 TEMPORARY: Creating fake ride request from driver_model_changed for popup testing")
                        
                        // Create a test ride request to trigger the popup
                        RideRequestedResponse(
                            rideId = "TEST_POPUP_${System.currentTimeMillis()}",
                            pickupLocation = com.piperrideshare.driver.api.models.response.websocket.Location(
                                latitude = 33.4942,
                                longitude = -111.9261,
                                address = "Scottsdale Fashion Square, Scottsdale, AZ"
                            ),
                            dropoffLocation = com.piperrideshare.driver.api.models.response.websocket.Location(
                                latitude = 33.4484,
                                longitude = -112.0740,
                                address = "Phoenix Sky Harbor Airport, Phoenix, AZ"
                            ),
                            estimatedFare = 45.50,
                            estimatedDistance = 12.5,
                            estimatedDuration = 18,
                            riderName = "John Doe",
                            rideType = "standard",
                            requestTime = "2025-07-16T01:27:07Z"
                        )
                    }
                    "ride_model_changed" -> {
                        val id = payload?.optString("id")
                        if (id != null) RideModelChangedResponse(id) else UnknownResponse(json.toString())
                    }
                    else -> UnknownResponse(json.toString())
                }

            "response" ->
                when (action) {
                    // Thomas Breakpoint: Set breakpoint here to debug go_online responses
                    "go_online" -> {
                        // @Thomas - BREAKPOINT HERE: Go online response received
                        println("🌐 WEBSOCKET: Go online response - Status: $status")
                        if (error.isNotEmpty()) {
                            println("❌ WEBSOCKET: Go online error - $error")
                        } else {
                            println("✅ WEBSOCKET: Go online successful")
                        }
                        // Create a response object for go_online
                        ActionResponse(type, action, status, error, payload)
                    }
                    "update_location" -> {
                        println("📍 WEBSOCKET: Update location response - Status: $status")
                        ActionResponse(type, action, status, error, payload)
                    }
                    "accept_ride" -> {
                        println("✅ WEBSOCKET: Accept ride response - Status: $status")
                        ActionResponse(type, action, status, error, payload)
                    }
                    "arrive_at_pickup" -> {
                        println("🚗 WEBSOCKET: Arrive at pickup response - Status: $status")
                        ActionResponse(type, action, status, error, payload)
                    }
                    "start_ride" -> {
                        println("🚀 WEBSOCKET: Start ride response - Status: $status")
                        ActionResponse(type, action, status, error, payload)
                    }
                    "complete_ride" -> {
                        println("🏁 WEBSOCKET: Complete ride response - Status: $status")
                        ActionResponse(type, action, status, error, payload)
                    }
                    else -> UnknownResponse(json.toString())
                }

            // Thomas Breakpoint: Set breakpoint here if getting unknown message types
            else -> UnknownResponse(json.toString())
        }
    }
}
