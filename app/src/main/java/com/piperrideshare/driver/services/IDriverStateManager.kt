package com.piperrideshare.driver.services.state

import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing persistent driver state across app sessions
 */
interface IDriverStateManager {
    val isOnline: Flow<Boolean>
    val currentRideId: Flow<String?>
    val lastLocation: Flow<Pair<Double, Double>?>
    val zoneId: Flow<String?>
    val rideTypeId: Flow<String?>
    val rideStatus: Flow<String?>

    suspend fun setOnlineState(
        isOnline: Boolean,
        latitude: Double? = null,
        longitude: Double? = null,
        zoneId: String? = null,
        rideTypeId: String? = null
    )

    suspend fun setCurrentRide(rideId: String?, status: String? = null)
    suspend fun updateLocation(latitude: Double, longitude: Double)
    suspend fun clearState()
    suspend fun getCurrentState(): DriverState

    /**
     * Data class representing current driver state
     */
    data class DriverState(
        val isOnline: Boolean,
        val currentRideId: String?,
        val lastLocation: Pair<Double, Double>?,
        val zoneId: String?,
        val rideTypeId: String?,
        val rideStatus: String?
    )
}