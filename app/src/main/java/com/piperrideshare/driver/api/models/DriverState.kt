package com.piperrideshare.driver.api.models

import com.piperrideshare.driver.api.models.response.websocket.DriverModelChangedResponse
import timber.log.Timber

/**
 * Driver availability states - matches backend DriverAvailabilityAggregate exactly
 *
 * These states come from the backend domain logic and should never be
 * managed locally. Always trust the backend state from driver_model_changed events.
 */
enum class DriverAvailabilityState {
    OFFLINE,    // Driver is not available for rides
    ONLINE,     // Driver is available and waiting for ride assignments
    EN_ROUTE,   // Driver is traveling to pickup location
    ARRIVED,    // Driver has arrived at pickup location
    IN_TRIP;    // Driver is currently in a trip with passenger

    companion object {
        fun fromString(state: String): DriverAvailabilityState {
            return when (state.uppercase()) {
                "OFFLINE" -> OFFLINE
                "ONLINE" -> ONLINE
                "EN_ROUTE" -> EN_ROUTE
                "ARRIVED" -> ARRIVED
                "IN_TRIP" -> IN_TRIP
                else -> {
                    // Log unknown state but default to OFFLINE for safety
                    Timber.w("Unknown driver availability state: $state, defaulting to OFFLINE")
                    OFFLINE
                }
            }
        }
    }
}

/**
 * Complete driver state - mirrors backend driver model
 * Note: We don't track connectionState - that's backend-only for routing decisions
 */
data class DriverState(
    val id: String,
    val availabilityState: DriverAvailabilityState,
    val currentRideId: String?,
    val currentLocation: Pair<Double, Double>?,

    // Additional fields from backend driver model
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val profilePhotoUrl: String = "",
    val isActive: Boolean = false,
    val isVerified: Boolean = false,
    val ratingAverage: Double = 0.0,
    val totalRides: Int = 0,
    val currentWeekEarnings: Int = 0,
    val availableBalance: Int = 0
) {

    /**
     * Helper methods to determine UI state based on availability
     * Note: We don't check connection state - if WebSocket works, we can send commands
     */
    fun isAvailable(): Boolean = availabilityState == DriverAvailabilityState.ONLINE

    fun isInRide(): Boolean = currentRideId?.isNotBlank() == true

    fun canGoOnline(): Boolean = availabilityState == DriverAvailabilityState.OFFLINE

    fun canGoOffline(): Boolean = availabilityState != DriverAvailabilityState.IN_TRIP

    fun canArriveAtPickup(): Boolean = availabilityState == DriverAvailabilityState.EN_ROUTE

    fun canStartRide(): Boolean = availabilityState == DriverAvailabilityState.ARRIVED

    fun canCompleteRide(): Boolean = availabilityState == DriverAvailabilityState.IN_TRIP

    /**
     * Get user-friendly state description
     */
    fun getStateDescription(): String {
        return when (availabilityState) {
            DriverAvailabilityState.OFFLINE -> "Offline"
            DriverAvailabilityState.ONLINE -> "Online - Waiting for rides"
            DriverAvailabilityState.EN_ROUTE -> "En route to pickup"
            DriverAvailabilityState.ARRIVED -> "Arrived at pickup"
            DriverAvailabilityState.IN_TRIP -> "In trip"
        }
    }

    /**
     * Get next action description for UI
     */
    fun getNextActionDescription(): String? {
        return when (availabilityState) {
            DriverAvailabilityState.OFFLINE -> "Go Online"
            DriverAvailabilityState.ONLINE -> "Go Offline"
            DriverAvailabilityState.EN_ROUTE -> "Arrive at Pickup"
            DriverAvailabilityState.ARRIVED -> "Start Ride"
            DriverAvailabilityState.IN_TRIP -> "Complete Ride"
        }
    }
}

/**
 * Extension function to convert backend driver model to local state
 * Note: We ignore connectionState - frontend doesn't need it
 */
fun DriverModelChangedResponse.toDriverState(): DriverState {
    return DriverState(
        id = this.driverId,
        availabilityState = DriverAvailabilityState.fromString(this.availabilityState),
        currentRideId = this.currentRideID.takeIf { it.isNotBlank() },
        currentLocation = this.currentLocation?.let { it.latitude to it.longitude },
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phone = this.phone,
        profilePhotoUrl = this.profilePhotoURL,
        isActive = this.isActive,
        isVerified = this.isVerified,
        ratingAverage = this.ratingAverage,
        totalRides = this.totalRides,
        currentWeekEarnings = this.currentWeekEarnings,
        availableBalance = this.availableBalance
    )
}