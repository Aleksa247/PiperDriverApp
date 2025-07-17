package com.piperrideshare.driver.domain.repository

import com.piperrideshare.driver.api.WebSocketHandler
import com.piperrideshare.driver.api.models.request.*
import com.piperrideshare.driver.api.models.response.websocket.WebSocketResponseParser
import com.piperrideshare.driver.data.network.WebSocketResult
import com.piperrideshare.driver.services.IWebSocketRepository
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketRepository
    @Inject
    constructor(
        private val webSocketHandler: WebSocketHandler,
    ) : IWebSocketRepository {
    override fun connect(
        token: String,
        onMessage: (Any) -> Unit,
    ) {
        webSocketHandler.connect(token) { result ->
            when (result) {
                is WebSocketResult.Message -> {
                    try {
                        val response = WebSocketResponseParser.parse(JSONObject(result.data))
                        onMessage(response)
                    } catch (e: Exception) {
                        onMessage(WebSocketResult.Failure("Parsing error: ${e.message}"))
                    }
                }
                else -> {
                    // Forward events like Connected, Failure, Disconnected, etc.
                    onMessage(result)
                }
            }
        }
    }

    override fun disconnect() {
        webSocketHandler.disconnect()
    }

    private fun sendRequest(request: WebSocketRequest) {
        webSocketHandler.send(request.toJson().toString())
    }

    override fun sendGoOnline(
        latitude: Double,
        longitude: Double,
        deviceId: String,
        zoneId: String,
        rideTypeId: String,
    ) {
        sendRequest(GoOnlineRequest(latitude, longitude, deviceId, zoneId, rideTypeId))
    }

    override fun sendUpdateLocation(latitude: Double, longitude: Double) {
        sendRequest(UpdateLocationRequest(latitude, longitude))
    }

    override fun sendAcceptRide(rideId: String) {
        sendRequest(AcceptRideRequest(rideId))
    }

    override fun sendArriveAtPickup(rideId: String) {
        sendRequest(ArriveAtPickupRequest(rideId))
    }

    override fun sendStartRide(rideId: String) {
        sendRequest(StartRideRequest(rideId))
    }

    override fun sendCompleteRide(rideId: String, distance: Double) {
        sendRequest(CompleteRideRequest(rideId, distance))
    }

    override fun sendGetActiveRide() {
        sendRequest(GetActiveRideRequest())
    }
}
