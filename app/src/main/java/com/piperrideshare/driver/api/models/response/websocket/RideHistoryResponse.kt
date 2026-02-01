package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.annotations.SerializedName

data class RideHistoryResponse(
    @SerializedName("payload")
    val rides: List<RideModelChangedResponse>?,
    @SerializedName("requestId")
    val requestId: String? = null
) : WebSocketResponse()
