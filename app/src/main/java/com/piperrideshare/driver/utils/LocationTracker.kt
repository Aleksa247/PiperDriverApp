package com.piperrideshare.driver.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double>? =
        try {
            // Create location request with high accuracy settings
            val locationRequest =
                LocationRequest
                    .Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setMinUpdateDistanceMeters(5.0f) // Minimum 5 meters between updates
                    .setMinUpdateIntervalMillis(5000) // Minimum 5 seconds between updates
                    .setMaxUpdateDelayMillis(10000) // Maximum 10 seconds delay
                    .build()

            // Get current location with high accuracy
            val location = fusedLocationClient.lastLocation.await()

            location?.let { currentLocation ->
                // Check if location is significantly different from last location
                if (isLocationSignificantlyDifferent(currentLocation)) {
                    lastLocation = currentLocation
                    Pair(currentLocation.latitude, currentLocation.longitude)
                } else {
                    // Location is too close to last location, try to get a new one
                    Timber.d("📍 LOCATION: Location too close to previous, requesting new location...")
                    getNewLocationWithRequest(locationRequest)
                }
            } ?: run {
                // No last location available, request a new one
                Timber.d("📍 LOCATION: No last location available, requesting new location...")
                getNewLocationWithRequest(locationRequest)
            }
        } catch (e: Exception) {
            Timber.e("❌ LOCATION ERROR: ${e.message}")
            e.printStackTrace()
            null
        }

    @SuppressLint("MissingPermission")
    private suspend fun getNewLocationWithRequest(locationRequest: LocationRequest): Pair<Double, Double>? =
        try {
            // Request a single location update with high accuracy
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

    @SuppressLint("DefaultLocale")
    private fun isLocationSignificantlyDifferent(newLocation: Location): Boolean {
        val lastLoc = lastLocation ?: return true // First location is always significant

        val distance = lastLoc.distanceTo(newLocation)
        val minDistanceMeters = 5.0 // Minimum 5 meters difference

        val isSignificant = distance >= minDistanceMeters
        if (!isSignificant) {
            Timber.d("📍 LOCATION: Location too close (${String.format("%.1f", distance)}m < ${minDistanceMeters}m)")
        }

        return isSignificant
    }

    /**
     * Force refresh location by clearing last location cache
     */
    fun clearLocationCache() {
        lastLocation = null
        Timber.d("📍 LOCATION: Location cache cleared")
    }
}
