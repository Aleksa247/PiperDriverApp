package com.piperrideshare.driver.api.models.response

class RideState {
    /**
     * Represents the current state of a ride.
     */
    enum class RideState {
        Idle, // No active ride
        EnRoute, // Driver is en route to pickup
        Arrived, // Driver has arrived at pickup
        InProgress, // Ride is in progress
        Completed, // Ride has been completed
    }
}
