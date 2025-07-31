package com.piperrideshare.driver.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Enhanced LocationTracker with better accuracy handling
 *
 * Handles location tracking with improved accuracy settings and
 * error handling for "too close" location delivery issues.
 *
 * @author Thomas Woodfin
 */
class LocationTracker(
    private val context: Context,
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var lastLocation: Location? = null
    private var locationCallback: LocationCallback? = null

    /**
     * One-time current location fetch (with accuracy and fallback)
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double>? =
        try {
            val locationRequest =
                LocationRequest
                    .Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setMinUpdateDistanceMeters(5.0f)
                    .setMinUpdateIntervalMillis(5000)
                    .setMaxUpdateDelayMillis(10000)
                    .build()

            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                if (isLocationSignificantlyDifferent(it)) {
                    lastLocation = it
                    Pair(it.latitude, it.longitude)
                } else {
                    Timber.d("📍 LOCATION: Too close to previous, requesting new location...")
                    getNewLocationWithRequest(locationRequest)
                }
            } ?: run {
                Timber.d("📍 LOCATION: No cached location, requesting fresh...")
                getNewLocationWithRequest(locationRequest)
            }
        } catch (e: Exception) {
            Timber.e("❌ LOCATION ERROR: ${e.message}")
            e.printStackTrace()
            null
        }

    /**
     * Request fresh single location
     */
    @SuppressLint("MissingPermission")
    private suspend fun getNewLocationWithRequest(locationRequest: LocationRequest): Pair<Double, Double>? =
        try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            location?.let {
                lastLocation = it
                Timber.d("📍 LOCATION: New location obtained - lat: ${it.latitude}, lng: ${it.longitude}")
                Pair(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            Timber.e("❌ LOCATION REQUEST ERROR: ${e.message}")
            e.printStackTrace()
            null
        }

    /**
     * Start live location updates using a callback
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(onLocationReceived: (Location) -> Unit) {
        val request =
            LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateDistanceMeters(5.0f)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(10000)
                .build()

        locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    if (isLocationSignificantlyDifferent(location)) {
                        lastLocation = location
                        onLocationReceived(location)
                    } else {
                        Timber.d("📍 LOCATION: Skipped update (too close)")
                    }
                }
            }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback!!,
            Looper.getMainLooper(),
        )

        Timber.d("📍 LOCATION: Started live location updates")
    }

    /**
     * Stop live updates
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
            Timber.d("📍 LOCATION: Stopped location updates")
        }
    }

    /**
     * Clear location cache (forces re-fetch)
     */
    fun clearLocationCache() {
        lastLocation = null
        Timber.d("📍 LOCATION: Cleared location cache")
    }

    /**
     * Checks if location differs enough from the last
     */
    private fun isLocationSignificantlyDifferent(newLocation: Location): Boolean {
        val lastLoc = lastLocation ?: return true
        val distance = lastLoc.distanceTo(newLocation)
        val minDistance = 5.0f
        val significant = distance >= minDistance
        if (!significant) {
            Timber.d("📍 LOCATION: Too close (${"%.1f".format(distance)}m < $minDistance m)")
        }
        return significant
    }
}
