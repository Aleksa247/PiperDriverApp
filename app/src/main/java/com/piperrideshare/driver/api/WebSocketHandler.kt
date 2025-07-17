package com.piperrideshare.driver.api

import com.piperrideshare.driver.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketHandler
    @Inject
    constructor() {
        private var webSocket: WebSocket? = null

        fun connect(
            token: String,
            onMessage: (String) -> Unit,
        ) {
            val client = OkHttpClient()
            val request =
                Request
                    .Builder()
                    .url("wss://${BuildConfig.BASE_URL}/api/drivers/ws")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

            webSocket =
                client.newWebSocket(
                    request,
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            println("WebSocket connected")
                        }

                        override fun onMessage(
                            webSocket: WebSocket,
                            text: String,
                        ) {
                            onMessage(text)
                        }

                        override fun onMessage(
                            webSocket: WebSocket,
                            bytes: ByteString,
                        ) {
                            onMessage(bytes.utf8())
                        }

                        override fun onFailure(
                            webSocket: WebSocket,
                            t: Throwable,
                            response: Response?,
                        ) {
                            println("WebSocket error: ${t.message}")
                        }

                        override fun onClosing(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            webSocket.close(1000, null)
                            println("WebSocket closing: $reason")
                        }

                        override fun onClosed(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            println("WebSocket closed: $reason")
                        }
                    },
                )
        }

        fun send(message: String) {
            if (webSocket != null) {
                webSocket?.send(message)
            } else {
                println("WebSocket is not connected. Cannot send message.")
            }
        }

        fun disconnect() {
            webSocket?.close(1000, "Client disconnected")
            webSocket = null
        }
    }
