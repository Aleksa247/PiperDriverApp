package com.piperrideshare.driver.services.state // Or the correct package for DriverStateManager

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


private val Context.driverStateDataStore by preferencesDataStore(name = "driver_state_prefs")

@Singleton
class DriverStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : IDriverStateManager {
    companion object {
        private val IS_ONLINE_KEY = booleanPreferencesKey("is_online")
        private val CURRENT_RIDE_ID_KEY = stringPreferencesKey("current_ride_id")
        private val LAST_LATITUDE_KEY = stringPreferencesKey("last_latitude")
        private val LAST_LONGITUDE_KEY = stringPreferencesKey("last_longitude")
        private val ZONE_ID_KEY = stringPreferencesKey("zone_id")
        private val RIDE_TYPE_ID_KEY = stringPreferencesKey("ride_type_id")
        private val RIDE_STATUS_KEY = stringPreferencesKey("ride_status")
    }

    override val isOnline: Flow<Boolean> = context.driverStateDataStore.data.map { prefs ->
        prefs[IS_ONLINE_KEY] ?: false
    }

    override val currentRideId: Flow<String?> = context.driverStateDataStore.data.map { prefs ->
        prefs[CURRENT_RIDE_ID_KEY]
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

    override val rideStatus: Flow<String?> = context.driverStateDataStore.data.map { prefs ->
        prefs[RIDE_STATUS_KEY]
    }

    override suspend fun setOnlineState(
        isOnline: Boolean,
        latitude: Double?,
        longitude: Double?,
        zoneId: String?,
        rideTypeId: String?
    ) {
        context.driverStateDataStore.edit { prefs ->
            prefs[IS_ONLINE_KEY] = isOnline
            latitude?.let { prefs[LAST_LATITUDE_KEY] = it.toString() }
                ?: prefs.remove(LAST_LATITUDE_KEY) // Also good to remove if null
            longitude?.let { prefs[LAST_LONGITUDE_KEY] = it.toString() }
                ?: prefs.remove(LAST_LONGITUDE_KEY)
            zoneId?.let { prefs[ZONE_ID_KEY] = it } ?: prefs.remove(ZONE_ID_KEY)
            rideTypeId?.let { prefs[RIDE_TYPE_ID_KEY] = it } ?: prefs.remove(RIDE_TYPE_ID_KEY)
        }
        Timber.d("💾 DRIVER STATE: Online state saved - isOnline: $isOnline")
    }

    override suspend fun setCurrentRide(rideId: String?, status: String?) {
        context.driverStateDataStore.edit { prefs ->
            if (rideId != null) {
                prefs[CURRENT_RIDE_ID_KEY] = rideId
            } else {
                prefs.remove(CURRENT_RIDE_ID_KEY)
            }
            if (status != null) {
                prefs[RIDE_STATUS_KEY] = status
            } else {
                prefs.remove(RIDE_STATUS_KEY)
            }
        }
        Timber.d("💾 DRIVER STATE: Ride state saved - rideId: $rideId, status: $status")
    }

    override suspend fun updateLocation(latitude: Double, longitude: Double) {
        context.driverStateDataStore.edit { prefs ->
            prefs[LAST_LATITUDE_KEY] = latitude.toString()
            prefs[LAST_LONGITUDE_KEY] = longitude.toString()
        }
    }

    override suspend fun clearState() {
        context.driverStateDataStore.edit { prefs ->
            prefs.clear()
        }
        Timber.d("💾 DRIVER STATE: All state cleared")
    }

    override suspend fun getCurrentState(): IDriverStateManager.DriverState { // <- Return type updated
        val prefs = context.driverStateDataStore.data.first()
        return IDriverStateManager.DriverState( // <- Instantiation updated
            isOnline = prefs[IS_ONLINE_KEY] ?: false,
            currentRideId = prefs[CURRENT_RIDE_ID_KEY],
            lastLocation = run {
                val lat = prefs[LAST_LATITUDE_KEY]?.toDoubleOrNull()
                val lng = prefs[LAST_LONGITUDE_KEY]?.toDoubleOrNull()
                if (lat != null && lng != null) lat to lng else null
            },
            zoneId = prefs[ZONE_ID_KEY],
            rideTypeId = prefs[RIDE_TYPE_ID_KEY],
            rideStatus = prefs[RIDE_STATUS_KEY]
        )
    }
}
