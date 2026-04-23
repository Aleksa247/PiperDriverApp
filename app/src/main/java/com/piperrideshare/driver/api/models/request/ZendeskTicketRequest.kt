package com.piperrideshare.driver.api.models.request

import com.google.gson.annotations.SerializedName

data class ZendeskTicketRequest(
    @SerializedName("request")
    val request: TicketData
)

data class TicketData(
    @SerializedName("subject")
    val subject: String,
    @SerializedName("requester")
    val requester: RequesterInfo,
    @SerializedName("comment")
    val comment: TicketComment
)

data class RequesterInfo(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String
)

data class TicketComment(
    @SerializedName("body")
    val body: String
)
