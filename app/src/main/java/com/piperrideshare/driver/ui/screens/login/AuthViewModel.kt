package com.piperrideshare.driver.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.api.models.response.AuthResponse
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
        private val _loginResult = MutableStateFlow<Result<AuthResponse>?>(null)
        val loginResult: StateFlow<Result<AuthResponse>?> = _loginResult
        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading

        fun login(
            email: String,
            password: String,
            deviceId: String,
        ) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val result = authRepository.login(email, password, deviceId)
                    if (result.isSuccess) {
                        result.getOrNull()?.let { response ->
                            sessionManager.saveAuthInfo(
                                token = response.token,
                                userId = response.userId,
                                name = response.name,
                            )
                        }
                    }
                    _loginResult.value = result
                } catch (e: Exception) {
                    _loginResult.value = Result.failure(e)
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
