package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ChatMessage(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("ride_id")
    val rideId: String,
    
    @SerializedName("from")
    val from: String, // "rider" or "driver"
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("timestamp")
    val timestamp: String, // ISO date string
)

data class ChatHistoryResponse(
    // The "payload" of the WebSocket response
    
    // Sometimes payload is a list directly, but consistent with other responses
    // iOS logic: response.payload -> [ChatMessagePayload]
    // Here we'll model it so WebSocketResponseParser can return it
    val messages: List<ChatMessage>
) : WebSocketResponse()

data class NewMessageResponse(
    // "type": "chat_message", "action": "new_message", "payload": { ... }
    val message: ChatMessage
) : WebSocketResponse()
