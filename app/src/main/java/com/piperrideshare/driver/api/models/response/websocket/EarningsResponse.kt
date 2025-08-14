package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.annotations.SerializedName

data class EarningsResponse(
    @SerializedName("requestId")
    val requestId: String? = null,
    @SerializedName("earnings")
    val earnings: Long? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("totalEarnings")
    val totalEarnings: Long? = null,
    @SerializedName("availableBalance")
    val availableBalance: Long? = null,
    @SerializedName("pendingBalance")
    val pendingBalance: Long? = null,
    @SerializedName("lastPayoutDate")
    val lastPayoutDate: String? = null,
    @SerializedName("currentWeekEarnings")
    val currentWeekEarnings: Long? = null,
    @SerializedName("currentMonthEarnings")
    val currentMonthEarnings: Long? = null
) : WebSocketResponse()