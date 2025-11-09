package com.piperrideshare.driver.domain.repository

import android.content.Context
import com.piperrideshare.driver.api.models.request.RideRequest
import com.piperrideshare.driver.services.MapboxSearchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RideRepository(
    private val context: Context,
) {

    private val mapboxSearchService = MapboxSearchService(context)

    suspend fun createRideRequest(
        rideId: String,
        riderId: String,
        pickupLat: Double,
        pickupLng: Double,
        dropoffLat: Double,
        dropoffLng: Double,
        estimatedFare: Double,
        estimatedDistance: Double,
        estimatedDuration: Int,
    ): RideRequest = withContext(Dispatchers.IO) {

        val pickupAddress = mapboxSearchService.reverseGeocode(pickupLat, pickupLng)
        val dropoffAddress = mapboxSearchService.reverseGeocode(dropoffLat, dropoffLng)

        RideRequest(
            rideId = rideId,
            riderId = riderId,
            pickupLatitude = pickupLat,
            pickupLongitude = pickupLng,
            pickupAddress = pickupAddress,
            dropoffLatitude = dropoffLat,
            dropoffLongitude = dropoffLng,
            dropoffAddress = dropoffAddress,
            estimatedFare = estimatedFare,
            estimatedDistance = estimatedDistance,
            estimatedDuration = estimatedDuration
        )
    }
}

