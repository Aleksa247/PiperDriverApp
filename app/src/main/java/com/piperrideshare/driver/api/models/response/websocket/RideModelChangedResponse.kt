package com.piperrideshare.driver.api.models.response.websocket

import com.google.gson.annotations.SerializedName

data class RideModelChangedResponse(
    @SerializedName("id") val rideId: String,
    @SerializedName("rider_id") val riderId: String,
    @SerializedName("driver_id") val driverId: String,
    val status: String,
    @SerializedName("pickup_location") val pickupLocation: LatLng?,
    @SerializedName("dropoff_location") val dropoffLocation: LatLng?,
    @SerializedName("current_location") val currentLocation: LatLng?,
    @SerializedName("rider_current_location") val riderCurrentLocation: LatLng?,
    @SerializedName("request_time") val requestTime: String?,
    @SerializedName("accept_time") val acceptTime: String?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("driver_arrival_time") val driverArrivalTime: String?,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("estimated_fare") val estimatedFare: Int,
    @SerializedName("driver_earning") val driverEarning: Int,
    @SerializedName("actual_fare") val actualFare: Int,
    @SerializedName("subtotal") val subtotal: Int? = null,
    @SerializedName("driver_cut_percent") val driverCutPercent: Double? = null,
    @SerializedName("wait_time_fee") val waitTimeFee: Int,
    @SerializedName("driver_wait_timer_end") val driverWaitTimerEnd: String?,
    val distance: Double,
    val duration: Int,
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("driver_tip") val driverTip: Int,
    @SerializedName("ride_type_id") val rideTypeId: String,
    @SerializedName("cancellation_fee") val cancellationFee: Int,
    @SerializedName("cancellation_type") val cancellationType: String,
    val rating: Int,
    val feedback: String,
    val version: Int,
    //styamamo-edit
    @SerializedName("pickup_address") val pickupAddress: String? = null,
    @SerializedName("dropoff_address") val dropoffAddress: String? = null,
) : WebSocketResponse()

data class LatLng(
    val latitude: Double,
    val longitude: Double,
)
