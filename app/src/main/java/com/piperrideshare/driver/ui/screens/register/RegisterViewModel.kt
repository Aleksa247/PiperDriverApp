package com.piperrideshare.driver.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.api.models.request.RegisterRequest
import com.piperrideshare.driver.api.models.response.AuthResponse
import com.piperrideshare.driver.api.models.response.Zone
import com.piperrideshare.driver.data.network.ApiResult
import com.piperrideshare.driver.domain.repository.AuthRepository
import com.piperrideshare.driver.services.session.ISessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: ISessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.LoadingZones)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _zones = MutableStateFlow<List<Zone>>(emptyList())
    val zones: StateFlow<List<Zone>> = _zones.asStateFlow()

    private val _registerResult = MutableStateFlow<ApiResult<AuthResponse>?>(null)
    val registerResult: StateFlow<ApiResult<AuthResponse>?> = _registerResult.asStateFlow()

    init {
        fetchZones()
    }

    fun fetchZones() {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.LoadingZones
            when (val result = authRepository.getZones()) {
                is ApiResult.Success -> {
                    _zones.value = result.data.zones.filter { it.isActive }
                    _uiState.value = RegisterUiState.Idle
                    Timber.d("✅ ZONES: Loaded ${_zones.value.size} active zones")
                }
                is ApiResult.Failure -> {
                    _uiState.value = RegisterUiState.Error("Failed to load zones: ${result.message}")
                    Timber.e("❌ ZONES: Failed to load zones - ${result.message}")
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = RegisterUiState.Error("Network error loading zones. Please check connection.")
                }
            }
        }
    }

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Registering
            
            // Save device ID for Login/Session use if needed
            // The logic in Login uses sessionManager.fcmToken.first() as deviceId ?? "Unknown"
            // We should ensure we are using the same device ID logic in the Screen/VM as Login
            
            val result = authRepository.register(request)
            _registerResult.value = result
            
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = RegisterUiState.Success
                    val response = result.data
                     // Save tokens
                    sessionManager.saveAuthInfo(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        expiresIn = response.expiresIn,
                        userId = response.id,
                        name = "${request.firstName} ${request.lastName}",
                    )
                    Timber.d("✅ REGISTER: Success for ${response.id}")
                }
                is ApiResult.Failure -> {
                    _uiState.value = RegisterUiState.Error(result.message)
                     Timber.e("❌ REGISTER: Failed - ${result.message}")
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = RegisterUiState.Error("Network error. Please check connection.")
                }
            }
        }
    }
    
    fun resetState() {
        _uiState.value = RegisterUiState.Idle
        _registerResult.value = null
    }
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object LoadingZones : RegisterUiState()
    object Registering : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
