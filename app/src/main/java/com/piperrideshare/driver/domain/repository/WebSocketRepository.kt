package com.piperrideshare.driver.domain.repository

import com.piperrideshare.driver.api.WebSocketHandler
import com.piperrideshare.driver.api.models.request.AcceptRideRequest
import com.piperrideshare.driver.api.models.request.ArriveAtPickupRequest
import com.piperrideshare.driver.api.models.request.CompleteRideRequest
import com.piperrideshare.driver.api.models.request.GetActiveRideRequest
import com.piperrideshare.driver.api.models.request.GoOnlineRequest
import com.piperrideshare.driver.api.models.request.StartRideRequest
import com.piperrideshare.driver.api.models.request.UpdateLocationRequest
import com.piperrideshare.driver.api.models.request.WebSocketRequest
import com.piperrideshare.driver.api.models.response.websocket.WebSocketResponseParser
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
            webSocketHandler.connect(token) { json ->
                val response = WebSocketResponseParser.parse(JSONObject(json))
                onMessage(response)
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

        override fun sendUpdateLocation(
            latitude: Double,
            longitude: Double,
        ) {
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

        override fun sendCompleteRide(
            rideId: String,
            distance: Double,
        ) {
            sendRequest(CompleteRideRequest(rideId, distance))
        }

        override fun sendGetActiveRide() {
            sendRequest(GetActiveRideRequest())
        }
    }
