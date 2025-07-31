package com.piperrideshare.driver.api

import com.piperrideshare.driver.BuildConfig
import com.piperrideshare.driver.data.network.WebSocketResult
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketHandler
    @Inject
    constructor() {
        private var webSocket: WebSocket? = null
        private var onEventCallback: ((WebSocketResult) -> Unit)? = null

        private var lastToken: String? = null
        private var autoReconnect = true
        private var reconnecting = false

        fun connect(
            token: String,
            onEvent: (WebSocketResult) -> Unit,
        ) {
            this.lastToken = token
            this.onEventCallback = onEvent
            this.reconnecting = false

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
                            Timber.d("✅ WebSocket connected")
                            onEvent(WebSocketResult.Connected)
                        }

                        override fun onMessage(
                            webSocket: WebSocket,
                            text: String,
                        ) {
                            Timber.d("📨 WebSocket message received: $text")
                            onEvent(WebSocketResult.Message(text))
                        }

                        override fun onMessage(
                            webSocket: WebSocket,
                            bytes: ByteString,
                        ) {
                            val text = bytes.utf8()
                            Timber.d("📨 WebSocket binary message received: $text")
                            onEvent(WebSocketResult.Message(text))
                        }

                        override fun onFailure(
                            webSocket: WebSocket,
                            t: Throwable,
                            response: Response?,
                        ) {
                            Timber.e("❌ WebSocket error: ${t.message}")
                            onEvent(WebSocketResult.Failure(t.message ?: "Unknown error"))
                            handleReconnect()
                        }

                        override fun onClosing(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            Timber.d("⚠️ WebSocket closing: $reason")
                            onEvent(WebSocketResult.Closing(reason))
                            webSocket.close(1000, null)
                        }

                        override fun onClosed(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            Timber.d("🔌 WebSocket closed: $reason")
                            onEvent(WebSocketResult.Closed(reason))
                            handleReconnect()
                        }
                    },
                )
        }

        private fun handleReconnect() {
            if (!autoReconnect || reconnecting || lastToken == null) return
            reconnecting = true

            Timber.d("🔁 Attempting to reconnect WebSocket...")

            // Optional: Add delay or exponential backoff here if needed
            connect(lastToken!!, onEventCallback ?: return)
        }

        fun send(message: String) {
            if (webSocket != null) {
                Timber.d("📤 Sending message: $message")
                webSocket?.send(message)
            } else {
                Timber.d("⚠️ WebSocket is not connected. Cannot send message.")
            }
        }

        fun disconnect() {
            autoReconnect = false
            reconnecting = false
            webSocket?.close(1000, "Client disconnected")
            webSocket = null
            Timber.d("🔌 WebSocket manually disconnected.")
            onEventCallback?.invoke(WebSocketResult.Disconnected)
        }

        fun isConnected(): Boolean = webSocket != null

        fun enableAutoReconnect(enable: Boolean) {
            autoReconnect = enable
        }
    }
