package com.piperrideshare.driver.services

interface IWebSocketRepository {
    fun connect(
        token: String,
        onMessage: (Any) -> Unit,
    )

    fun disconnect()

    fun sendGoOnline(
        latitude: Double,
        longitude: Double,
        deviceId: String,
        zoneId: String,
        rideTypeId: String,
    )

    fun sendUpdateLocation(
        latitude: Double,
        longitude: Double,
    )

    fun sendAcceptRide(rideId: String)

    fun sendArriveAtPickup(rideId: String)

    fun sendStartRide(rideId: String)

    fun sendCompleteRide(
        rideId: String,
        distance: Double,
    )

    fun sendGetActiveRide()
    fun sendGetRideHistory(requestId: String)
    fun sendGoOffline()
}
