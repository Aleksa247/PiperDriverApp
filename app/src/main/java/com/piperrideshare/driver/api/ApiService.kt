package com.piperrideshare.driver.api

import com.piperrideshare.driver.api.models.request.CompleteEmailVerificationRequest
import com.piperrideshare.driver.api.models.request.CompletePhoneVerificationRequest
import com.piperrideshare.driver.api.models.request.InitiateEmailVerificationRequest
import com.piperrideshare.driver.api.models.request.InitiatePhoneVerificationRequest
import com.piperrideshare.driver.api.models.request.InitializeStripeRequest
import com.piperrideshare.driver.api.models.request.LoginRequest
import com.piperrideshare.driver.api.models.request.RefreshTokenRequest
import com.piperrideshare.driver.api.models.request.RegisterRequest
import com.piperrideshare.driver.api.models.response.AuthResponse
import com.piperrideshare.driver.api.models.response.OnboardingStatusResponse
import com.piperrideshare.driver.api.models.response.RefreshTokenResponse
import com.piperrideshare.driver.api.models.response.StripeLinkResponse
import com.piperrideshare.driver.api.models.response.ZonesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {
    // Auth
    @POST("/api/drivers/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): AuthResponse

    @POST("/api/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest,
    ): RefreshTokenResponse

    // Onboarding
    @GET("/api/drivers/onboarding-status")
    suspend fun getOnboardingStatus(): OnboardingStatusResponse

    // Phone Verification
    @POST("/api/drivers/verify/phone/initiate")
    suspend fun initiatePhoneVerification(
        @Body request: InitiatePhoneVerificationRequest,
    )

    @POST("/api/drivers/verify/phone/complete")
    suspend fun completePhoneVerification(
        @Body request: CompletePhoneVerificationRequest,
    )

    // Email Verification
    @POST("/api/drivers/verify/email/initiate")
    suspend fun initiateEmailVerification(
        @Body request: InitiateEmailVerificationRequest,
    )

    @POST("/api/drivers/verify/email/complete")
    suspend fun completeEmailVerification(
        @Body request: CompleteEmailVerificationRequest,
    )

    // Stripe
    @POST("/api/drivers/stripe/initialize")
    suspend fun initializeStripe(
        @Body request: InitializeStripeRequest,
    )

    @GET("/api/drivers/stripe-link")
    suspend fun getStripeLink(
        @Query("return_url") returnUrl: String = "https://www.thepiper.co/driver/onboarding/success",
    ): StripeLinkResponse

    // Registration
    @POST("/api/drivers/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): AuthResponse

    @GET
    suspend fun getZones(@Url url: String): ZonesResponse
}


