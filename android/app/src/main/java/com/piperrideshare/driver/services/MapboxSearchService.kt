package com.piperrideshare.driver.services

import android.content.Context

/**
 * MapboxSearchService is temporarily disabled due to missing Mapbox Search SDK dependencies.
 * This service would handle address search and reverse geocoding functionality.
 */
class MapboxSearchService(private val context: Context) {
    // TODO: Implement when Mapbox Search SDK is added to dependencies
    
    suspend fun searchAddresses(query: String): List<String> {
        // Placeholder implementation
        return emptyList()
    }
    
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String {
        // Placeholder implementation
        return "Unknown location"
    }
}