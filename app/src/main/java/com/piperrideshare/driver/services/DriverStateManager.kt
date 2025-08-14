package com.piperrideshare.driver.services

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.piperrideshare.driver.api.models.DriverAvailabilityState
import com.piperrideshare.driver.api.models.DriverState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.driverStateDataStore by preferencesDataStore(name = "driver_state_prefs")

/**
 * Simplified DriverStateManager - Just stores backend state
 *
 * No more complex state management or boolean flags.
 * This is just persistent storage for the backend driver state.
 */
@Singleton
class DriverStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : IDriverStateManager {

    private val gson = Gson()

    companion object {
        private val DRIVER_STATE_KEY = stringPreferencesKey("driver_state")
        private val LAST_LATITUDE_KEY = stringPreferencesKey("last_latitude")
        private val LAST_LONGITUDE_KEY = stringPreferencesKey("last_longitude")
        private val ZONE_ID_KEY = stringPreferencesKey("zone_id")
        private val RIDE_TYPE_ID_KEY = stringPreferencesKey("ride_type_id")
    }

    // ==============================================
    // INTERFACE IMPLEMENTATION
    // ==============================================

    override val driverState: Flow<DriverState?> = context.driverStateDataStore.data.map { prefs ->
        prefs[DRIVER_STATE_KEY]?.let { json ->
            try {
                gson.fromJson(json, DriverState::class.java)
            } catch (e: Exception) {
                Timber.e("Error parsing driver state: ${e.message}")
                null
            }
        }
    }

    override val availabilityState: Flow<DriverAvailabilityState> = driverState.map { state ->
        state?.availabilityState ?: DriverAvailabilityState.OFFLINE
    }

    override val currentRideId: Flow<String?> = driverState.map { state ->
        state?.currentRideId
    }

    override val lastLocation: Flow<Pair<Double, Double>?> = context.driverStateDataStore.data.map { prefs ->
        val lat = prefs[LAST_LATITUDE_KEY]?.toDoubleOrNull()
        val lng = prefs[LAST_LONGITUDE_KEY]?.toDoubleOrNull()
        if (lat != null && lng != null) lat to lng else null
    }

    override val zoneId: Flow<String?> = context.driverStateDataStore.data.map { prefs ->
        prefs[ZONE_ID_KEY]
    }

    override val rideTypeId: Flow<String?> = context.driverStateDataStore.data.map { prefs ->
        prefs[RIDE_TYPE_ID_KEY]
    }

    override suspend fun saveDriverState(driverState: DriverState) {
        context.driverStateDataStore.edit { prefs ->
            prefs[DRIVER_STATE_KEY] = gson.toJson(driverState)
        }
        Timber.d("💾 DRIVER STATE: Saved backend state - ${driverState.availabilityState}, RideId: ${driverState.currentRideId}")
    }

    override suspend fun saveOnlinePreferences(zoneId: String, rideTypeId: String) {
        context.driverStateDataStore.edit { prefs ->
            prefs[ZONE_ID_KEY] = zoneId
            prefs[RIDE_TYPE_ID_KEY] = rideTypeId
        }
        Timber.d("💾 DRIVER STATE: Saved online preferences - Zone: $zoneId, RideType: $rideTypeId")
    }

    override suspend fun updateLocation(latitude: Double, longitude: Double) {
        context.driverStateDataStore.edit { prefs ->
            prefs[LAST_LATITUDE_KEY] = latitude.toString()
            prefs[LAST_LONGITUDE_KEY] = longitude.toString()
        }
        // Also update the driver state if it exists
        val currentState = getCurrentState()
        currentState?.let { state ->
            val updatedState = state.copy(currentLocation = latitude to longitude)
            saveDriverState(updatedState)
        }
    }

    override suspend fun clearState() {
        context.driverStateDataStore.edit { prefs ->
            prefs.clear()
        }
        Timber.d("💾 DRIVER STATE: All state cleared")
    }

    override suspend fun getCurrentState(): DriverState? {
        return driverState.first()
    }

    // ==============================================
    // LEGACY SUPPORT METHODS (Optional - for gradual migration)
    // ==============================================

    /**
     * Legacy method for backward compatibility
     * Use driverState.availabilityState instead
     */
    @Deprecated("Use driverState.availabilityState instead")
    val isOnline: Flow<Boolean> = availabilityState.map { it == DriverAvailabilityState.ONLINE }

    /**
     * Legacy method for backward compatibility
     * Use driverState.getStateDescription() instead
     */
    @Deprecated("Use driverState.getStateDescription() instead")
    val rideStatus: Flow<String?> = availabilityState.map { state ->
        when (state) {
            DriverAvailabilityState.OFFLINE -> null
            DriverAvailabilityState.ONLINE -> "online"
            DriverAvailabilityState.EN_ROUTE -> "accepted"
            DriverAvailabilityState.ARRIVED -> "driver_arrived"
            DriverAvailabilityState.IN_TRIP -> "in_progress"
        }
    }

    /**
     * Legacy method - Use saveDriverState() instead
     */
    @Deprecated("Use saveDriverState() instead")
    suspend fun setOnlineState(
        isOnline: Boolean,
        latitude: Double? = null,
        longitude: Double? = null,
        zoneId: String? = null,
        rideTypeId: String? = null
    ) {
        val currentState = getCurrentState()
        if (currentState != null) {
            val newState = currentState.copy(
                availabilityState = if (isOnline) DriverAvailabilityState.ONLINE else DriverAvailabilityState.OFFLINE,
                currentLocation = if (latitude != null && longitude != null) latitude to longitude else currentState.currentLocation
            )
            saveDriverState(newState)
        }

        if (zoneId != null && rideTypeId != null) {
            saveOnlinePreferences(zoneId, rideTypeId)
        }

        if (latitude != null && longitude != null) {
            updateLocation(latitude, longitude)
        }
    }

    /**
     * Legacy method - Use saveDriverState() instead
     */
    @Deprecated("Use saveDriverState() instead")
    suspend fun setCurrentRide(rideId: String?, status: String? = null) {
        val currentState = getCurrentState()
        if (currentState != null) {
            val newAvailabilityState = when (status) {
                "accepted" -> DriverAvailabilityState.EN_ROUTE
                "driver_arrived" -> DriverAvailabilityState.ARRIVED
                "in_progress" -> DriverAvailabilityState.IN_TRIP
                null -> if (rideId == null) DriverAvailabilityState.ONLINE else currentState.availabilityState
                else -> currentState.availabilityState
            }

            val newState = currentState.copy(
                currentRideId = rideId,
                availabilityState = newAvailabilityState
            )
            saveDriverState(newState)
        }
    }

    // ==============================================
    // HELPER METHODS
    // ==============================================

    /**
     * Check if driver can go online based on current state
     */
    suspend fun canGoOnline(): Boolean {
        return getCurrentState()?.canGoOnline() ?: true
    }

    /**
     * Check if driver can go offline based on current state
     */
    suspend fun canGoOffline(): Boolean {
        return getCurrentState()?.canGoOffline() ?: true
    }

    /**
     * Check if driver is in an active ride
     */
    suspend fun isInRide(): Boolean {
        return getCurrentState()?.isInRide() ?: false
    }

    /**
     * Get user-friendly state description
     */
    suspend fun getStateDescription(): String {
        return getCurrentState()?.getStateDescription() ?: "Unknown"
    }

    /**
     * Get next action description for UI
     */
    suspend fun getNextActionDescription(): String? {
        return getCurrentState()?.getNextActionDescription()
    }
}