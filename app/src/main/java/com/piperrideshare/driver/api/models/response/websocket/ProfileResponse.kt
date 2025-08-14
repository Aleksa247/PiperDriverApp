package com.piperrideshare.driver.api.models.response.websocket


import com.google.gson.annotations.SerializedName

/**
 * Response for get_profile query
 */


data class ProfileResponse(
    @SerializedName("requestId")
    val requestId: String? = null,
    @SerializedName("payload")
    val payload: ProfilePayload? = null
) : WebSocketResponse()

data class ProfilePayload(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("profilePhotoURL")
    val profilePhotoUrl: String? = null,
    @SerializedName("isActive")
    val isActive: Boolean? = null,
    @SerializedName("isVerified")
    val isVerified: Boolean? = null,
    @SerializedName("ratingAverage")
    val ratingAverage: Double? = null,
    @SerializedName("totalRides")
    val totalRides: Int? = null,
    @SerializedName("currentWeekEarnings")
    val currentWeekEarnings: Int? = null,
    @SerializedName("availableBalance")
    val availableBalance: Int? = null,
    @SerializedName("currentVehicleInfo")
    val currentVehicleInfo: VehicleInfo? = null
)
