package com.piperrideshare.driver.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.api.models.response.websocket.DriverModelChangedResponse
import com.piperrideshare.driver.api.models.response.websocket.RideModelChangedResponse
import com.piperrideshare.driver.api.models.response.websocket.RideRequestedResponse
import com.piperrideshare.driver.api.models.response.websocket.UnknownResponse
import com.piperrideshare.driver.services.IWebSocketRepository
import com.piperrideshare.driver.services.session.ISessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebSocketViewModel
    @Inject
    constructor(
        private val repository: IWebSocketRepository,
        private val sessionManager: ISessionManager,
    ) : ViewModel() {
        private val _rideRequest = MutableStateFlow<RideRequestedResponse?>(null)
        val rideRequest = _rideRequest.asStateFlow()

        private val _driverModel = MutableStateFlow<DriverModelChangedResponse?>(null)
        val driverModel = _driverModel.asStateFlow()

        private val _rideModel = MutableStateFlow<RideModelChangedResponse?>(null)
        val rideModel = _rideModel.asStateFlow()

        init {
            viewModelScope.launch {
                try {
                    sessionManager.token.first()?.let { token ->
                        connect(token)
                    }
                } catch (e: Exception) {
                    println("WebSocketViewModel init failed: ${e.message}")
                }
            }
        }

        private fun connect(token: String) {
            repository.connect(token) { response ->
                viewModelScope.launch {
                    when (response) {
                        is RideRequestedResponse -> _rideRequest.value = response
                        is DriverModelChangedResponse -> _driverModel.value = response
                        is RideModelChangedResponse -> _rideModel.value = response
                        is UnknownResponse -> println("Unhandled: ${response.raw}")
                    }
                }
            }
        }

        fun disconnect() {
            repository.disconnect()
        }

        fun goOnline(
            latitude: Double,
            longitude: Double,
            deviceId: String,
            zoneId: String,
            rideTypeId: String,
        ) {
            viewModelScope.launch {
                sessionManager.token.first()?.let {
                    repository.sendGoOnline(latitude, longitude, deviceId, zoneId, rideTypeId)
                }
            }
        }

        fun updateLocation(
            latitude: Double,
            longitude: Double,
        ) {
            repository.sendUpdateLocation(latitude, longitude)
        }

        fun acceptRide(rideId: String) {
            repository.sendAcceptRide(rideId)
        }

        fun arriveAtPickup(rideId: String) {
            repository.sendArriveAtPickup(rideId)
        }

        fun startRide(rideId: String) {
            repository.sendStartRide(rideId)
        }

        fun completeRide(
            rideId: String,
            distance: Double,
        ) {
            repository.sendCompleteRide(rideId, distance)
        }

        fun getActiveRide() {
            repository.sendGetActiveRide()
        }
    }
