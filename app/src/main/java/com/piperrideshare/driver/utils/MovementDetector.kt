package com.piperrideshare.driver.utils

import android.location.Location
import timber.log.Timber
import kotlin.math.abs

/**
 * Movement detection utility for determining if driver is stationary or moving
 * 
 * Uses location history and speed analysis to determine movement state
 */
class MovementDetector {
    
    private val locationHistory = mutableListOf<LocationPoint>()
    private val maxHistorySize = 3 // Keep last 3 location points
    private val minMovementThreshold = 2.0f // meters
    private val speedThreshold = 1.0f // m/s (approximately 3.6 km/h)
    
    data class LocationPoint(
        val latitude: Double,
        val longitude: Double,
        val timestamp: Long,
        val speed: Float? = null
    )
    
    /**
     * Add a new location point and determine if driver is moving
     * @param latitude Current latitude
     * @param longitude Current longitude
     * @param speed Speed in m/s if available from GPS
     * @return true if driver is moving, false if stationary
     */
    fun isMoving(latitude: Double, longitude: Double, speed: Float? = null): Boolean {
        val currentTime = System.currentTimeMillis()
        val currentPoint = LocationPoint(latitude, longitude, currentTime, speed)
        
        // Add to history
        locationHistory.add(currentPoint)
        
        // Keep only recent history
        while (locationHistory.size > maxHistorySize) {
            locationHistory.removeAt(0)
        }
        
        // Need at least 2 points to detect movement
        if (locationHistory.size < 2) {
            Timber.d("🚶 MOVEMENT: Not enough location history, assuming stationary")
            return false
        }
        
        // Check 1: Speed-based detection (if GPS provides speed)
        speed?.let { currentSpeed ->
            if (currentSpeed > speedThreshold) {
                Timber.d("🚗 MOVEMENT: Moving based on speed: ${currentSpeed}m/s")
                return true
            }
        }
        
        // Check 2: Distance-based detection
        val isMovingByDistance = isMovingByDistance()
        
        // Check 3: Consistency check (movement over multiple points)
        val isConsistentMovement = isConsistentMovement()
        
        val finalResult = isMovingByDistance || isConsistentMovement
        
        Timber.d("🎯 MOVEMENT: Distance=${isMovingByDistance}, Consistent=${isConsistentMovement}, Final=${finalResult}")
        
        return finalResult
    }
    
    /**
     * Check if driver has moved based on distance between recent points
     */
    private fun isMovingByDistance(): Boolean {
        if (locationHistory.size < 2) return false
        
        val latest = locationHistory.last()
        val previous = locationHistory[locationHistory.size - 2]
        
        val distance = calculateDistance(
            previous.latitude, previous.longitude,
            latest.latitude, latest.longitude
        )
        
        val timeDiff = (latest.timestamp - previous.timestamp) / 1000.0 // seconds
        val speed = if (timeDiff > 0) distance / timeDiff else 0.0
        
        Timber.d("📏 MOVEMENT: Distance=${distance}m, Time=${timeDiff}s, Speed=${speed}m/s")
        
        return distance > minMovementThreshold
    }
    
    /**
     * Check for consistent movement pattern over multiple location points
     */
    private fun isConsistentMovement(): Boolean {
        if (locationHistory.size < 3) return false
        
        var movingCount = 0
        
        // Check movement between consecutive points
        for (i in 1 until locationHistory.size) {
            val current = locationHistory[i]
            val previous = locationHistory[i - 1]
            
            val distance = calculateDistance(
                previous.latitude, previous.longitude,
                current.latitude, current.longitude
            )
            
            if (distance > minMovementThreshold) {
                movingCount++
            }
        }
        
        // Consider moving if majority of recent samples show movement
        val threshold = (locationHistory.size - 1) / 2
        return movingCount >= threshold
    }
    
    /**
     * Calculate distance between two points in meters
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
    
    /**
     * Get current movement state description for debugging
     */
    fun getMovementState(): String {
        if (locationHistory.isEmpty()) return "No data"
        
        val latest = locationHistory.last()
        return "Points: ${locationHistory.size}, Speed: ${latest.speed}m/s"
    }
    
    /**
     * Clear location history (useful when driver goes offline/online)
     */
    fun clearHistory() {
        locationHistory.clear()
        Timber.d("🧹 MOVEMENT: Cleared location history")
    }
}