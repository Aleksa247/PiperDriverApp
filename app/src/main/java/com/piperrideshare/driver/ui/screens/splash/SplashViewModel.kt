package com.piperrideshare.driver.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.services.session.ISessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: ISessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashState>(SplashState.Loading)
    val uiState: StateFlow<SplashState> = _uiState

    fun checkSession() {
        viewModelScope.launch {
            val isExpired = sessionManager.isTokenExpired()
            if (isExpired) {
                _uiState.value = SplashState.NavigateToLogin
            } else {
                _uiState.value = SplashState.NavigateToHome
            }
        }
    }
}

sealed class SplashState {
    object Loading : SplashState()
    object NavigateToLogin : SplashState()
    object NavigateToHome : SplashState()
}
