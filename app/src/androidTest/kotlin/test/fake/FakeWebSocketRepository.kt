package test.fake

import com.piperrideshare.driver.services.IWebSocketRepository

class FakeWebSocketRepository : IWebSocketRepository {
    var connectedToken: String? = null
    var sentGoOnlineCalls = mutableListOf<GoOnlineCall>()

    data class GoOnlineCall(
        val latitude: Double,
        val longitude: Double,
        val deviceId: String,
        val zoneId: String,
        val rideTypeId: String,
    )

    private var onMessageCallback: ((Any) -> Unit)? = null

    override suspend fun connect(
        token: String,
        onMessage: (Any) -> Unit,
    ) {
        connectedToken = token
        onMessageCallback = onMessage
    }

    override fun disconnect() {
        connectedToken = null
        onMessageCallback = null
    }

    override fun sendGoOnline(
        latitude: Double,
        longitude: Double,
        deviceId: String,
        zoneId: String,
        rideTypeId: String,
    ) {
        sentGoOnlineCalls.add(GoOnlineCall(latitude, longitude, deviceId, zoneId, rideTypeId))
    }

    override fun sendUpdateLocation(
        latitude: Double,
        longitude: Double,
    ) {}

    override fun sendAcceptRide(rideId: String) {}

    override fun sendArriveAtPickup(rideId: String) {}

    override fun sendStartRide(rideId: String) {}

    override fun sendCompleteRide(
        rideId: String,
        distance: Double,
    ) {}

    override fun sendGetActiveRide() {}

    fun triggerFakeResponse(response: Any) {
        onMessageCallback?.invoke(response)
    }
}