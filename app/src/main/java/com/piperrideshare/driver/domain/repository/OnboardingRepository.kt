package com.piperrideshare.driver.domain.repository

import com.piperrideshare.driver.api.ApiService
import com.piperrideshare.driver.api.models.request.CompleteEmailVerificationRequest
import com.piperrideshare.driver.api.models.request.CompletePhoneVerificationRequest
import com.piperrideshare.driver.api.models.request.InitiateEmailVerificationRequest
import com.piperrideshare.driver.api.models.request.InitiatePhoneVerificationRequest
import com.piperrideshare.driver.api.models.request.InitializeStripeRequest
import com.piperrideshare.driver.api.models.response.OnboardingStatusResponse
import com.piperrideshare.driver.data.network.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for onboarding-related API calls.
 */
@Singleton
class OnboardingRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getOnboardingStatus(): ApiResult<OnboardingStatusResponse> {
        return try {
            val response = apiService.getOnboardingStatus()
            ApiResult.Success(response)
        } catch (e: retrofit2.HttpException) {
            ApiResult.Failure(e.message ?: "HTTP error ${e.code()}", e.code())
        } catch (e: Exception) {
            ApiResult.NetworkError
        }
    }

    suspend fun initiatePhoneVerification(phone: String): ApiResult<Unit> {
        return try {
            apiService.initiatePhoneVerification(InitiatePhoneVerificationRequest(phone))
            ApiResult.Success(Unit)
        } catch (e: retrofit2.HttpException) {
            ApiResult.Failure(e.message ?: "HTTP error ${e.code()}", e.code())
        } catch (e: Exception) {
            ApiResult.NetworkError
        }
    }

    suspend fun completePhoneVerification(phone: String, code: String): ApiResult<Unit> {
        return try {
            apiService.completePhoneVerification(CompletePhoneVerificationRequest(phone, code))
            ApiResult.Success(Unit)
        } catch (e: retrofit2.HttpException) {
            ApiResult.Failure(e.message ?: "HTTP error ${e.code()}", e.code())
        } catch (e: Exception) {
            ApiResult.NetworkError
        }
    }

    suspend fun initiateEmailVerification(email: String): ApiResult<Unit> {
        return try {
            apiService.initiateEmailVerification(InitiateEmailVerificationRequest(email))
            ApiResult.Success(Unit)
        } catch (e: retrofit2.HttpException) {
            ApiResult.Failure(e.message ?: "HTTP error ${e.code()}", e.code())
        } catch (e: Exception) {
            ApiResult.NetworkError
        }
    }

    suspend fun completeEmailVerification(code: String): ApiResult<Unit> {
        return try {
            apiService.completeEmailVerification(CompleteEmailVerificationRequest(code))
            ApiResult.Success(Unit)
        } catch (e: retrofit2.HttpException) {
            ApiResult.Failure(e.message ?: "HTTP error ${e.code()}", e.code())
        } catch (e: Exception) {
            ApiResult.NetworkError
        }
    }

    suspend fun initializeStripe(email: String): ApiResult<Unit> {
        return try {
            apiService.initializeStripe(InitializeStripeRequest(email = email))
            ApiResult.Success(Unit)
        } catch (e: retrofit2.HttpException) {
            ApiResult.Failure(e.message ?: "HTTP error ${e.code()}", e.code())
        } catch (e: Exception) {
            ApiResult.NetworkError
        }
    }

    suspend fun getStripeLink(): ApiResult<String> {
        return try {
            val response = apiService.getStripeLink()
            ApiResult.Success(response.onboardingUrl)
        } catch (e: retrofit2.HttpException) {
            ApiResult.Failure(e.message ?: "HTTP error ${e.code()}", e.code())
        } catch (e: Exception) {
            ApiResult.NetworkError
        }
    }
}

