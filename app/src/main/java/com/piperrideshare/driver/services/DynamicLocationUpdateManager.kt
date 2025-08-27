package com.piperrideshare.driver.services

import android.content.Context
import com.piperrideshare.driver.api.models.DriverAvailabilityState
import com.piperrideshare.driver.utils.LocationTracker
import com.piperrideshare.driver.utils.MovementDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dynamic Location Update Manager
 * 
 * Handles location updates with different intervals based on:
 * - Driver availability state (OFFLINE, ONLINE, EN_ROUTE, ARRIVED, IN_TRIP)
 * - Movement detection (stationary vs moving)
 * 
 * Update Intervals:
 * - OFFLINE: 90 seconds
 * - ONLINE + moving: 5 seconds
 * - ONLINE + stationary: 15 seconds  
 * - EN_ROUTE/ARRIVED/IN_TRIP + moving: 3 seconds
 * - EN_ROUTE/ARRIVED/IN_TRIP + stationary: 5 seconds
 */
@Singleton
class DynamicLocationUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val locationTracker = LocationTracker(context)
    private val movementDetector = MovementDetector()
    
    private var updateJob: Job? = null
    private var isActive = false
    
    private var currentDriverState: DriverAvailabilityState = DriverAvailabilityState.OFFLINE
    private var onLocationUpdateCallback: ((Double, Double) -> Unit)? = null
    
    companion object {
        // Update intervals in milliseconds
        private const val OFFLINE_INTERVAL = 90_000L // 90 seconds
        private const val ONLINE_MOVING_INTERVAL = 5_000L // 5 seconds
        private const val ONLINE_STATIONARY_INTERVAL = 15_000L // 15 seconds
        private const val IN_RIDE_MOVING_INTERVAL = 3_000L // 3 seconds
        private const val IN_RIDE_STATIONARY_INTERVAL = 5_000L // 5 seconds
    }
    
    /**
     * Start dynamic location updates
     * @param scope CoroutineScope to run the updates in
     * @param onLocationUpdate Callback when location is updated
     */
    fun startLocationUpdates(
        scope: CoroutineScope,
        onLocationUpdate: (Double, Double) -> Unit
    ) {
        if (isActive) return
        
        isActive = true
        onLocationUpdateCallback = onLocationUpdate
        
        updateJob = scope.launch {
            Timber.d("🎯 DYNAMIC LOCATION: Starting dynamic location updates")
            
            while (isActive) {
                try {
                    val location = locationTracker.getCurrentLocation()
                    location?.let { (lat, lng) ->
                        // Check if driver is moving
                        val isMoving = movementDetector.isMoving(lat, lng)
                        
                        // Calculate next update interval
                        val interval = calculateUpdateInterval(currentDriverState, isMoving)
                        
                        // Send location update
                        onLocationUpdateCallback?.invoke(lat, lng)
                        
                        Timber.d("📍 DYNAMIC LOCATION: Updated location (${lat}, ${lng}) - State: ${currentDriverState}, Moving: ${isMoving}, Next: ${interval/1000}s")
                        
                        // Wait for calculated interval
                        delay(interval)
                        
                    } ?: run {
                        Timber.w("⚠️ DYNAMIC LOCATION: Failed to get location, retrying in 10s")
                        delay(10_000)
                    }
                    
                } catch (e: Exception) {
                    Timber.e("❌ DYNAMIC LOCATION ERROR: ${e.message}")
                    delay(10_000) // Retry in 10 seconds on error
                }
            }
        }
    }
    
    /**
     * Stop location updates
     */
    fun stopLocationUpdates() {
        isActive = false
        updateJob?.cancel()
        updateJob = null
        onLocationUpdateCallback = null
        
        // Clear movement history when stopping
        movementDetector.clearHistory()
        
        Timber.d("🛑 DYNAMIC LOCATION: Stopped location updates")
    }
    
    /**
     * Update driver availability state
     * This affects the update intervals
     */
    fun updateDriverState(state: DriverAvailabilityState) {
        val previousState = currentDriverState
        currentDriverState = state
        
        // Clear movement history when state changes significantly
        if (shouldClearMovementHistory(previousState, state)) {
            movementDetector.clearHistory()
        }
        
        Timber.d("🔄 DYNAMIC LOCATION: Driver state changed from ${previousState} to ${state}")
    }
    
    /**
     * Calculate update interval based on driver state and movement
     */
    private fun calculateUpdateInterval(state: DriverAvailabilityState, isMoving: Boolean): Long {
        return when (state) {
            DriverAvailabilityState.OFFLINE -> {
                OFFLINE_INTERVAL
            }
            DriverAvailabilityState.ONLINE -> {
                if (isMoving) ONLINE_MOVING_INTERVAL else ONLINE_STATIONARY_INTERVAL
            }
            DriverAvailabilityState.EN_ROUTE,
            DriverAvailabilityState.ARRIVED,
            DriverAvailabilityState.IN_TRIP -> {
                if (isMoving) IN_RIDE_MOVING_INTERVAL else IN_RIDE_STATIONARY_INTERVAL
            }
        }
    }
    
    /**
     * Determine if movement history should be cleared on state change
     */
    private fun shouldClearMovementHistory(
        previousState: DriverAvailabilityState,
        newState: DriverAvailabilityState
    ): Boolean {
        // Clear history when transitioning between major state categories
        return when {
            previousState == DriverAvailabilityState.OFFLINE && newState != DriverAvailabilityState.OFFLINE -> true
            previousState != DriverAvailabilityState.OFFLINE && newState == DriverAvailabilityState.OFFLINE -> true
            previousState == DriverAvailabilityState.ONLINE && isInRideState(newState) -> true
            isInRideState(previousState) && newState == DriverAvailabilityState.ONLINE -> true
            else -> false
        }
    }
    
    /**
     * Check if state is related to active ride
     */
    private fun isInRideState(state: DriverAvailabilityState): Boolean {
        return state in listOf(
            DriverAvailabilityState.EN_ROUTE,
            DriverAvailabilityState.ARRIVED,
            DriverAvailabilityState.IN_TRIP
        )
    }
    
    /**
     * Get current update interval for debugging
     */
    fun getCurrentInterval(isMoving: Boolean): Long {
        return calculateUpdateInterval(currentDriverState, isMoving)
    }
    
    /**
     * Get status information for debugging
     */
    fun getStatusInfo(): String {
        return "State: $currentDriverState, Active: $isActive, Movement: ${movementDetector.getMovementState()}"
    }
}