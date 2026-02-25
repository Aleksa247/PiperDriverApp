package com.piperrideshare.driver.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class SplashViewModel @Inject constructor(
    private val sessionManager: ISessionManager,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashState>(SplashState.Loading)
    val uiState: StateFlow<SplashState> = _uiState

    fun checkSession() {
        viewModelScope.launch {
            val isExpired = sessionManager.isTokenExpired()
            if (!isExpired) {
                Timber.d("✅ SESSION: Access token still valid")
                _uiState.value = SplashState.NavigateToHome
                return@launch
            }

            // Token expired — attempt silent refresh
            val refreshToken = sessionManager.refreshToken.first()
            if (refreshToken.isNullOrBlank()) {
                Timber.d("🔒 SESSION: No refresh token available, navigating to login")
                _uiState.value = SplashState.NavigateToLogin
                return@launch
            }

            Timber.d("🔄 SESSION: Access token expired, attempting refresh...")
            when (val result = authRepository.refreshToken(refreshToken)) {
                is ApiResult.Success -> {
                    Timber.d("✅ SESSION: Token refresh successful")
                    sessionManager.saveAuthInfo(
                        accessToken = result.data.accessToken,
                        refreshToken = result.data.refreshToken,
                        expiresIn = result.data.expiresIn,
                        userId = null,
                        name = null,
                    )
                    _uiState.value = SplashState.NavigateToHome
                }
                is ApiResult.Failure -> {
                    Timber.e("❌ SESSION: Token refresh failed - ${result.message}")
                    _uiState.value = SplashState.NavigateToLogin
                }
                is ApiResult.NetworkError -> {
                    Timber.e("🌐 SESSION: Network error during refresh")
                    _uiState.value = SplashState.NavigateToLogin
                }
            }
        }
    }
}

sealed class SplashState {
    object Loading : SplashState()
    object NavigateToLogin : SplashState()
    object NavigateToHome : SplashState()
}

