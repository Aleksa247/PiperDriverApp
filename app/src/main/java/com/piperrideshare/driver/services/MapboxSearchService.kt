package com.piperrideshare.driver.services

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * MapboxSearchService is temporarily disabled due to missing Mapbox Search SDK dependencies.
 * This service would handle address search and reverse geocoding functionality.
 */
class MapboxSearchService(
    private val context: Context,
) {
    // TODO: Implement when Mapbox Search SDK is added to dependencies

    suspend fun searchAddresses(query: String): List<String> {
        // Placeholder implementation
        return emptyList()
    }

    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double,
    ): String = withContext(Dispatchers.IO) {
        try{
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<android.location.Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            // if address is not empty store in results and prefer a full addressline not just coordinates
            if(!addresses.isNullOrEmpty()){
                val address = addresses[0]
                address.getAddressLine(0) ?: listOfNotNull(address.locality, address.adminArea).joinToString(", ")
            } else {
                "Unknown Location"
            }
        } catch (e: Exception){
            e.printStackTrace()
            "Unknown Location"
        }
    }
}
