package com.piperrideshare.driver.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.api.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val authService: AuthService,
    ) : ViewModel() {
        private val _loginResult = MutableStateFlow<Result<com.piperrideshare.driver.api.models.response.AuthResponse>?>(null)
        val loginResult: StateFlow<Result<com.piperrideshare.driver.api.models.response.AuthResponse>?> = _loginResult

        fun login(
            email: String,
            password: String,
            deviceId: String,
        ) {
            viewModelScope.launch {
                val result = authService.login(email, password, deviceId)
                _loginResult.value = result
            }
        }
    }
