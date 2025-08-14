package com.piperrideshare.driver.services

import com.piperrideshare.driver.api.models.DriverAvailabilityState
import com.piperrideshare.driver.api.models.DriverState
import kotlinx.coroutines.flow.Flow

/**
 * Simplified DriverStateManager - now just stores backend state
 *
 * No more complex state management or boolean flags.
 * This is just persistent storage for the backend driver state.
 */
interface IDriverStateManager {

    // Core state from backend
    val driverState: Flow<DriverState?>
    val availabilityState: Flow<DriverAvailabilityState>
    val currentRideId: Flow<String?>

    // Last known location for app restart scenarios
    val lastLocation: Flow<Pair<Double, Double>?>

    // Zone/ride type for going online after restart
    val zoneId: Flow<String?>
    val rideTypeId: Flow<String?>

    /**
     * Save complete backend driver state - called when driver_model_changed received
     */
    suspend fun saveDriverState(driverState: DriverState)

    /**
     * Save zone/ride type info for going online after restart
     */
    suspend fun saveOnlinePreferences(zoneId: String, rideTypeId: String)

    /**
     * Update location for periodic tracking
     */
    suspend fun updateLocation(latitude: Double, longitude: Double)

    /**
     * Clear all state (logout)
     */
    suspend fun clearState()

    /**
     * Get current state synchronously for initialization
     */
    suspend fun getCurrentState(): DriverState?
}