package com.piperrideshare.driver.api.models.request

import com.google.gson.annotations.SerializedName

/**
 * Request to fetch chat history for a ride
 */
/**
 * Request to fetch chat history for a ride
 */
data class GetChatHistoryRequest(
    @SerializedName("requestId")
    val requestId: String,
    
    @SerializedName("payload")
    val payload: ChatHistoryPayload
) : WebSocketRequest {
    override val type: String = "query"
    override val action: String = "get_chat_history"

    data class ChatHistoryPayload(
        @SerializedName("ride_id")
        val rideId: String
    )
    
    override fun toJson(): org.json.JSONObject {
        val json = org.json.JSONObject()
        json.put("type", type)
        json.put("action", action)
        val payloadJson = org.json.JSONObject()
        payloadJson.put("ride_id", payload.rideId)
        json.put("payload", payloadJson)
        json.put("requestId", requestId)
        return json
    }
}

/**
 * Request to send a chat message
 */
data class SendChatMessageRequest(
    @SerializedName("payload")
    val payload: ChatMessagePayload
) : WebSocketRequest {
    override val type: String = "command"
    override val action: String = "send_message"

    data class ChatMessagePayload(
        @SerializedName("ride_id")
        val rideId: String,
        
        @SerializedName("message")
        val message: String
    )
    
    override fun toJson(): org.json.JSONObject {
        val json = org.json.JSONObject()
        json.put("type", type)
        json.put("action", action)
        val payloadJson = org.json.JSONObject()
        payloadJson.put("ride_id", payload.rideId)
        payloadJson.put("message", payload.message)
        json.put("payload", payloadJson)
        return json
    }
}
