package com.piperrideshare.driver.api.models.response

import com.google.gson.annotations.SerializedName

/**
 * Response from /api/drivers/stripe-link
 */
data class StripeLinkResponse(
    @SerializedName("onboarding_url")
    val onboardingUrl: String,
)
