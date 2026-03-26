package com.piperrideshare.driver.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.api.models.response.OnboardingStatusResponse
import com.piperrideshare.driver.data.network.ApiResult
import com.piperrideshare.driver.domain.repository.OnboardingRepository
import com.piperrideshare.driver.services.session.ISessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * OnboardingViewModel - Manages onboarding state and verification flows.
 * Mirrors iOS OnboardingViewModel structure.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val sessionManager: ISessionManager,
) : ViewModel() {

    private val _onboardingStatus = MutableStateFlow<OnboardingStatusResponse?>(null)
    val onboardingStatus: StateFlow<OnboardingStatusResponse?> = _onboardingStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    /**
     * Fetch current onboarding status from backend.
     */
    fun fetchOnboardingStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = onboardingRepository.getOnboardingStatus()) {
                is ApiResult.Success -> {
                    Timber.d("📋 Onboarding status: ${result.data.nextSteps}")
                    _onboardingStatus.value = result.data
                }
                is ApiResult.Failure -> {
                    _errorMessage.value = "Failed to get status: ${result.message}"
                }
                is ApiResult.NetworkError -> {
                    _errorMessage.value = "Network error: Please check your connection"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Initiate phone verification (send SMS code).
     */
    fun initiatePhoneVerification(phone: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            when (val result = onboardingRepository.initiatePhoneVerification(phone)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Verification code sent to $phone"
                }
                is ApiResult.Failure -> {
                    _errorMessage.value = "Failed to send code: ${result.message}"
                }
                is ApiResult.NetworkError -> {
                    _errorMessage.value = "Network error"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Complete phone verification with code.
     */
    fun completePhoneVerification(phone: String, code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = onboardingRepository.completePhoneVerification(phone, code)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Phone verified!"
                    fetchOnboardingStatus()
                    onSuccess()
                }
                is ApiResult.Failure -> {
                    _errorMessage.value = "Invalid verification code"
                }
                is ApiResult.NetworkError -> {
                    _errorMessage.value = "Network error"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Initiate email verification (send email code).
     */
    fun initiateEmailVerification(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            when (val result = onboardingRepository.initiateEmailVerification(email)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Verification code sent to $email"
                }
                is ApiResult.Failure -> {
                    _errorMessage.value = "Failed to send code: ${result.message}"
                }
                is ApiResult.NetworkError -> {
                    _errorMessage.value = "Network error"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Complete email verification with code.
     */
    fun completeEmailVerification(code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = onboardingRepository.completeEmailVerification(code)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Email verified!"
                    fetchOnboardingStatus()
                    onSuccess()
                }
                is ApiResult.Failure -> {
                    _errorMessage.value = "Invalid verification code"
                }
                is ApiResult.NetworkError -> {
                    _errorMessage.value = "Network error"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Initialize Stripe and get onboarding URL.
     */
    fun setupStripe(email: String, onUrl: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // First initialize
            when (val initResult = onboardingRepository.initializeStripe(email)) {
                is ApiResult.Success -> {
                    // Then get link
                    when (val linkResult = onboardingRepository.getStripeLink()) {
                        is ApiResult.Success -> {
                            onUrl(linkResult.data)
                        }
                        is ApiResult.Failure -> {
                            _errorMessage.value = "Failed to get Stripe link"
                        }
                        is ApiResult.NetworkError -> {
                            _errorMessage.value = "Network error"
                        }
                    }
                }
                is ApiResult.Failure -> {
                    // Might already be initialized, try getting link anyway
                    when (val linkResult = onboardingRepository.getStripeLink()) {
                        is ApiResult.Success -> {
                            onUrl(linkResult.data)
                        }
                        is ApiResult.Failure -> {
                            _errorMessage.value = "Failed to setup Stripe"
                        }
                        is ApiResult.NetworkError -> {
                            _errorMessage.value = "Network error"
                        }
                    }
                }
                is ApiResult.NetworkError -> {
                    _errorMessage.value = "Network error"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Continue Stripe onboarding (for incomplete accounts).
     */
    fun continueStripeOnboarding(onUrl: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = onboardingRepository.getStripeLink()) {
                is ApiResult.Success -> {
                    onUrl(result.data)
                }
                is ApiResult.Failure -> {
                    _errorMessage.value = "Failed to get Stripe link"
                }
                is ApiResult.NetworkError -> {
                    _errorMessage.value = "Network error"
                }
            }

            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun getCurrentStep(): String? = _onboardingStatus.value?.getCurrentStep()

    fun isComplete(): Boolean = _onboardingStatus.value?.isComplete() ?: false
}
