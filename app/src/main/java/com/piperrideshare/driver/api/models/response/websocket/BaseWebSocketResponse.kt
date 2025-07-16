package com.piperrideshare.driver.api.models.response.websocket

import org.json.JSONObject

/**
 * Base class for all WebSocket responses
 *
 * Provides a common interface for different types of WebSocket messages
 * received from the ride-sharing server.
 *
 * @author Thomas Woodfin
 */
sealed class WebSocketResponse

/**
 * Response for WebSocket action responses (go_online, update_location, etc.)
 *
 * Handles responses to actions sent by the driver app, including
 * success/error status and any error messages.
 *
 * @param type The message type (usually "response")
 * @param action The action that was performed (e.g., "go_online")
 * @param status The response status ("success" or "error")
 * @param error Any error message if status is "error"
 * @param payload Additional response data (can be null)
 *
 * @author Thomas Woodfin
 */
data class ActionResponse(
    val type: String,
    val action: String,
    val status: String,
    val error: String,
    val payload: JSONObject?,
) : WebSocketResponse()
