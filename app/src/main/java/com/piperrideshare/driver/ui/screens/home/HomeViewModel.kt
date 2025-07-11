package com.piperrideshare.driver.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the HomeScreen, managing driver state and basic functionality.
 */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val _isOnline = MutableStateFlow(false)
        val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

        private val _earnings = MutableStateFlow(0.0)
        val earnings: StateFlow<Double> = _earnings.asStateFlow()

        fun toggleOnlineStatus() {
            _isOnline.value = !_isOnline.value
        }

        fun updateEarnings(newEarnings: Double) {
            _earnings.value = newEarnings
        }
    }
