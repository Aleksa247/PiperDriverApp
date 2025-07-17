package com.piperrideshare.driver.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class LocationTracker(
    private val context: Context,
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double>? =
        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                Pair(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
}
