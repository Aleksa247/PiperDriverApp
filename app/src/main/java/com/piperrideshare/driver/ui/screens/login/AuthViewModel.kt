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
import kotlinx.coroutines.launch
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
        deviceId: String,
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            // @Thomas - BREAKPOINT HERE: About to make API call to login endpoint
            // This will show in logcat when the actual HTTP request is made
            println("🌐 API CALL: Making login request to /api/drivers/login")
            println("📧 Email: $email")
            println("🔑 Device ID: $deviceId")

            // Perform actual API login
            val result = authRepository.login(email, password, deviceId)

            // @Thomas - BREAKPOINT HERE: API response received
            // Check logcat for HTTP response details
            when (result) {
                is ApiResult.Success -> {
                    val response = result.data
                    println("✅ LOGIN SUCCESS: Token=${response.token}")
                    sessionManager.saveAuthInfo(
                        token = response.token,
                        userId = response.userId,
                        name = response.name
                    )
                }
                is ApiResult.Failure -> {
                    println("❌ LOGIN FAILURE: ${result.message} (code=${result.code})")
                }
                is ApiResult.NetworkError -> {
                    println("🌐 NETWORK ERROR: Please check your connection.")
                }
            }

            _loginResult.value = result
            _isLoading.value = false
        }
    }
}
