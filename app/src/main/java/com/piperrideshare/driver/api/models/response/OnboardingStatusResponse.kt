package com.piperrideshare.driver.api.models.response

import com.google.gson.annotations.SerializedName

/**
 * Response from /api/drivers/onboarding-status
 * Matches iOS OnboardingStatusResponse
 */
data class OnboardingStatusResponse(
    @SerializedName("driver_id")
    val driverId: String,
    
    @SerializedName("email_verified")
    val emailVerified: Boolean,
    
    @SerializedName("phone_verified")
    val phoneVerified: Boolean,
    
    @SerializedName("verification_status")
    val verificationStatus: String,
    
    @SerializedName("verification_required")
    val verificationRequired: Boolean,
    
    @SerializedName("stripe_account_status")
    val stripeAccountStatus: String,
    
    @SerializedName("stripe_setup_required")
    val stripeSetupRequired: Boolean,
    
    @SerializedName("can_stripe_onboard")
    val canStripeOnboard: Boolean,
    
    @SerializedName("can_go_online")
    val canGoOnline: Boolean,
    
    @SerializedName("next_steps")
    val nextSteps: List<String>,
) {
    /**
     * Onboarding steps as defined by the backend:
     * - VERIFY_EMAIL
     * - VERIFY_PHONE
     * - COMPLETE_CHECKR_INVITATION
     * - WAIT_FOR_BACKGROUND_CHECK
     * - UNDER_REVIEW
     * - VERIFICATION_REJECTED
     * - SETUP_STRIPE
     * - COMPLETE_STRIPE_ONBOARDING
     * - READY_TO_GO_ONLINE
     */
    fun getCurrentStep(): String? = nextSteps.firstOrNull()
    
    fun isComplete(): Boolean = canGoOnline
}
