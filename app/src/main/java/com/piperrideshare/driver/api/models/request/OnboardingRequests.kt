package com.piperrideshare.driver.api.models.request

import com.google.gson.annotations.SerializedName

data class InitiatePhoneVerificationRequest(
    @SerializedName("phone")
    val phone: String,
)

data class CompletePhoneVerificationRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("code")
    val code: String,
)

data class InitiateEmailVerificationRequest(
    @SerializedName("email")
    val email: String,
)

data class CompleteEmailVerificationRequest(
    @SerializedName("code")
    val code: String,
)

data class InitializeStripeRequest(
    @SerializedName("business_type")
    val businessType: String = "individual",
    @SerializedName("country")
    val country: String = "US",
    @SerializedName("email")
    val email: String,
    @SerializedName("account_type")
    val accountType: String = "express",
)
