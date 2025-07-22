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

        fun connect(
            token: String,
            onEvent: (WebSocketResult) -> Unit,
        ) {
            onEventCallback = onEvent

            val client = OkHttpClient()
            val request =
                Request
                    .Builder()
                    .url("wss://${BuildConfig.BASE_URL}/api/drivers/ws")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

            // @Thomas - BREAKPOINT HERE: Inspect request URL and headers before connection starts
            webSocket =
                client.newWebSocket(
                    request,
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            Timber.d("✅ WebSocket connected")
                            // @Thomas - BREAKPOINT HERE: Connection opened; inspect response headers if needed
                            onEvent(WebSocketResult.Connected)
                        }

                        override fun onMessage(
                            webSocket: WebSocket,
                            text: String,
                        ) {
                            Timber.d("📨 WebSocket message received: $text")
                            // @Thomas - BREAKPOINT HERE: Received text message from server; check message content
                            onEvent(WebSocketResult.Message(text))
                        }

                        override fun onMessage(
                            webSocket: WebSocket,
                            bytes: ByteString,
                        ) {
                            val text = bytes.utf8()
                            Timber.d("📨 WebSocket binary message received: $text")
                            // @Thomas - BREAKPOINT HERE: Binary message converted to text; validate conversion
                            onEvent(WebSocketResult.Message(text))
                        }

                        override fun onFailure(
                            webSocket: WebSocket,
                            t: Throwable,
                            response: Response?,
                        ) {
                            Timber.e("❌ WebSocket error: ${t.message}")
                            // @Thomas - BREAKPOINT HERE: Inspect error details and possible response from server
                            onEvent(WebSocketResult.Failure(t.message ?: "Unknown error"))
                        }

                        override fun onClosing(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            Timber.d("⚠️ WebSocket closing: $reason")
                            // @Thomas - BREAKPOINT HERE: Graceful shutdown started; inspect reason/code
                            onEvent(WebSocketResult.Closing(reason))
                            webSocket.close(1000, null)
                        }

                        override fun onClosed(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            Timber.d("🔌 WebSocket closed: $reason")
                            // @Thomas - BREAKPOINT HERE: WebSocket closed completely; log reason
                            onEvent(WebSocketResult.Closed(reason))
                        }
                    },
                )
        }

        fun send(message: String) {
            if (webSocket != null) {
                Timber.d("📤 Sending message: $message")
                // @Thomas - BREAKPOINT HERE: Outgoing message; verify structure and content
                webSocket?.send(message)
            } else {
                Timber.d("⚠️ WebSocket is not connected. Cannot send message.")
                // @Thomas - BREAKPOINT HERE: Attempted to send while disconnected; check connection state
            }
        }

        fun disconnect() {
            webSocket?.close(1000, "Client disconnected")
            webSocket = null
            Timber.d("🔌 WebSocket manually disconnected.")
            // @Thomas - BREAKPOINT HERE: Manual disconnect triggered; check if cleanup is needed
            onEventCallback?.invoke(WebSocketResult.Disconnected)
        }

        private fun onEvent(result: WebSocketResult) {
            // @Thomas - BREAKPOINT HERE: Final dispatch of WebSocket event to ViewModel/UI
            onEventCallback?.invoke(result)
        }
    }
