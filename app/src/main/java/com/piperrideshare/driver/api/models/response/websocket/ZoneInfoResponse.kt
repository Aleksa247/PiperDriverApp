package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.annotations.SerializedName

/**
 * ZoneInfoResponse - WebSocket response for zone information
 *
 * This response is sent to drivers when they connect to the backend,
 * providing zone-specific information including available ride types,
 * vehicle requirements, and pricing details.
 *
 * @author Thomas Woodfin
 */
data class ZoneInfoResponse(
    val type: String,
    val action: String,
    val payload: ZoneInfoPayload,
) : WebSocketResponse()

data class ZoneInfoPayload(
    val zone: Zone,
)

data class Zone(
    val id: String,
    val name: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("center_lat")
    val centerLat: Double,
    @SerializedName("center_lng")
    val centerLng: Double,
    @SerializedName("zone_bounds")
    val zoneBounds: List<ZoneBound>,
    @SerializedName("CenterLocation")
    val centerLocation: CenterLocation,
    @SerializedName("ride_type_ids")
    val rideTypeIds: List<String>,
    @SerializedName("vehicle_types")
    val vehicleTypes: List<VehicleType>,
    val requirements: ZoneRequirements,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
)

data class ZoneBound(
    val latitude: Double,
    val longitude: Double,
)

data class CenterLocation(
    @SerializedName("Type")
    val type: String,
    @SerializedName("Coordinates")
    val coordinates: List<Double>,
)

data class VehicleType(
    val id: String,
    val name: String,
    val description: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("base_fare")
    val baseFare: Double,
    @SerializedName("fare_per_mile")
    val farePerMile: Double,
    @SerializedName("fare_per_minute")
    val farePerMinute: Double,
    @SerializedName("min_vehicle_year")
    val minVehicleYear: Int,
    @SerializedName("required_features")
    val requiredFeatures: List<String>,
    @SerializedName("allowed_ownership_types")
    val allowedOwnershipTypes: String,
    @SerializedName("required_documents")
    val requiredDocuments: List<String>,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
)

data class ZoneRequirements(
    @SerializedName("min_driver_age")
    val minDriverAge: Int,
    @SerializedName("valid_states")
    val validStates: List<String>,
    @SerializedName("min_vehicle_year")
    val minVehicleYear: Int,
    @SerializedName("required_documents")
    val requiredDocuments: List<String>,
)
