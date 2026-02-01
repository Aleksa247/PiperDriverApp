package com.piperrideshare.driver.services

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
/**
 * H3Service - Converts GPS coordinates to H3 geospatial index
 *
 * Uses Cloudflare Worker to convert lat/lng to Uber H3 hexagonal grid indices
 * for zone-based routing on the backend.
 *
 * @author Sanket Agarwal
 */
@Singleton
class H3Service @Inject constructor() {
    private val client = OkHttpClient()
    private val h3WorkerUrl = "https://h3-finder.braden-160.workers.dev"

    /**
     * Convert latitude and longitude to H3 index
     *
     * @param lat Latitude (-90 to 90)
     * @param lng Longitude (-180 to 180)
     * @param resolution H3 resolution (0-15, default: 9)
     * @return H3 index string or null if conversion fails
     */
    suspend fun getH3Index(
        lat: Double,
        lng: Double,
        resolution: Int = 9
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "$h3WorkerUrl/h3?lat=$lat&lng=$lng&resolution=$resolution"
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Timber.e("❌ H3 API error: ${response.code}")
                return@withContext null
            }

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)

            val h3Index = json.getString("h3_index")
            Timber.d("✅ H3 index for ($lat, $lng): $h3Index")

            h3Index
        } catch (e: Exception) {
            Timber.e("❌ H3 conversion error: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}