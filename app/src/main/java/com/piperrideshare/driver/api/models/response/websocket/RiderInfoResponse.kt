package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.annotations.SerializedName

data class RiderInfoResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
) : WebSocketResponse()
