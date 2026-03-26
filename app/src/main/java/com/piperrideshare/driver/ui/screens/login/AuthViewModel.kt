package com.piperrideshare.driver.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.api.models.response.AuthResponse
import com.piperrideshare.driver.data.network.ApiResult
import com.piperrideshare.driver.domain.repository.AuthRepository
import com.piperrideshare.driver.services.session.ISessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val sessionManager: ISessionManager,
    ) : ViewModel() {
        private val _loginResult = MutableStateFlow<ApiResult<AuthResponse>?>(null)
        val loginResult: StateFlow<ApiResult<AuthResponse>?> = _loginResult
        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading

        fun login(
            email: String,
            password: String,
        ) {
            viewModelScope.launch {
                _isLoading.value = true

                // @Thomas - BREAKPOINT HERE: Retrieve FCM token from local session
                val deviceId = sessionManager.fcmToken.first() ?: "Unknown"

                // @Thomas - BREAKPOINT HERE: About to make API call to login endpoint
                Timber.d("🌐 API CALL: Making login request to /api/drivers/login")
                Timber.d("📧 Email: $email")
                Timber.d("🔑 Device ID: $deviceId")

                // Perform actual API login
                val result = authRepository.login(email, password, deviceId)

                // @Thomas - BREAKPOINT HERE: API response received
                when (result) {
                    is ApiResult.Success -> {
                        val response = result.data
                        Timber.d("✅ LOGIN SUCCESS: ID=${response.id}, ExpiresIn=${response.expiresIn}s")
                        
                        // Save tokens using new session manager interface
                        sessionManager.saveAuthInfo(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken,
                            expiresIn = response.expiresIn,
                            userId = response.id,
                            name = response.name ?: response.id, // Fallback to ID if name not provided
                        )
                    }

                    is ApiResult.Failure -> {
                        Timber.e("❌ LOGIN FAILURE: ${result.message} (code=${result.code})")
                    }
                    is ApiResult.NetworkError -> {
                        Timber.e("🌐 NETWORK ERROR: Please check your connection.")
                    }
                }

                _loginResult.value = result
                _isLoading.value = false
            }
        }

        /**
         * Reset login state - call this when entering the login screen after logout
         * to prevent stale success state from triggering auto-navigation
         */
        fun resetLoginState() {
            _loginResult.value = null
            _isLoading.value = false
        }
    }

