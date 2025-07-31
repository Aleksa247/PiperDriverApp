package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.annotations.SerializedName

data class DriverModelChangedResponse(
    @SerializedName("id") val driverId: String,
    @SerializedName("availabilityState") val availabilityState: String,
    @SerializedName("connectionState") val connectionState: String,
    @SerializedName("currentRideID") val currentRideID: String,
    @SerializedName("currentLocation") val currentLocation: DriverLocation?,
    @SerializedName("availableBalance") val availableBalance: Int,
    @SerializedName("currentWeekEarnings") val currentWeekEarnings: Int,
    @SerializedName("email") val email: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("profilePhotoURL") val profilePhotoURL: String,
    @SerializedName("ratingAverage") val ratingAverage: Double,
    @SerializedName("totalRides") val totalRides: Int,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("isVerified") val isVerified: Boolean,
    @SerializedName("currentVehicleInfo") val currentVehicleInfo: VehicleInfo?
) : WebSocketResponse()

data class DriverLocation(
    val latitude: Double,
    val longitude: Double
)

data class VehicleInfo(
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    @SerializedName("license_plate") val licensePlate: String,
    @SerializedName("photo_urls") val photoUrls: List<String>?,
    @SerializedName("ownership_type") val ownershipType: String,
    @SerializedName("ride_type_id") val rideTypeId: String,
    @SerializedName("is_rental") val isRental: Boolean,
    val vin: String,
    @SerializedName("insurance_provider") val insuranceProvider: String,
    @SerializedName("insurance_policy_number") val insurancePolicyNumber: String
)