package com.piperrideshare.driver.services

import android.content.Context
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.geojson.Point
import com.piperrideshare.driver.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * MapboxSearchService is temporarily disabled due to missing Mapbox Search SDK dependencies.
 * This service would handle address search and reverse geocoding functionality.
 */
@Singleton
class MapboxSearchService @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun searchAddresses(query: String): List<String> {
        // Placeholder implementation
        return emptyList()
    }

    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double,
    ): String = withContext(Dispatchers.IO) {
        try {
            val accessToken = context.getString(R.string.mapbox_access_token)
            
            val client = MapboxGeocoding.builder()
                .accessToken(accessToken)
                .query(Point.fromLngLat(longitude, latitude))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS, GeocodingCriteria.TYPE_POI)
                .limit(1)
                .build()

            val response = client.executeCall()
            
            if (response.isSuccessful && response.body() != null) {
                val features = response.body()!!.features()
                if (!features.isNullOrEmpty()) {
                    return@withContext features[0].placeName() ?: "Unknown Location"
                }
            } else {
                Timber.e("Mapbox Geocoding failed: ${response.message()}")
            }
            "Unknown Location"
        } catch (e: Exception) {
            Timber.e(e, "Error during reverse geocoding")
            "Unknown Location"
        }
    }
}
