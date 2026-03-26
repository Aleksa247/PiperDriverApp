package com.piperrideshare.driver.api.models.request

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("middle_name") val middleName: String = "",
    @SerializedName("last_name") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String,
    @SerializedName("dob") val dob: String, // ISO8601 or yyyy-MM-dd
    @SerializedName("operational_zone_id") val operationalZoneId: String,
    @SerializedName("street") val street: String,
    @SerializedName("city") val city: String,
    @SerializedName("state") val state: String,
    @SerializedName("zip_code") val zipCode: String,
    @SerializedName("country") val country: String = "US",
    @SerializedName("device_id") val deviceId: String
)
