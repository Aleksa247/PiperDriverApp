package com.piperrideshare.driver.api.models.response

import com.google.gson.annotations.SerializedName

data class ZonesResponse(
    @SerializedName("zones") val zones: List<Zone>
)

data class Zone(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("is_active") val isActive: Boolean
)
