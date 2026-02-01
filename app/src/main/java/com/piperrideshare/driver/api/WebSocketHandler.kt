package com.piperrideshare.driver.api

import com.piperrideshare.driver.data.local.DebugSettingsManager
import com.piperrideshare.driver.data.network.WebSocketResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

/**
 * WebSocketHandler with exponential backoff for reconnection.
 *
 * The backend enforces single-session for drivers. If a driver reconnects too quickly
 * (e.g., after network glitch), the old session may still be alive and the backend
 * returns 409 Conflict. This handler uses exponential backoff to give the backend
 * time to clear the old session.
 */
@Singleton
class WebSocketHandler
    @Inject
    constructor(
        private val debugSettingsManager: DebugSettingsManager
    ) {
        private var webSocket: WebSocket? = null
        private var onEventCallback: ((WebSocketResult) -> Unit)? = null
        private var lastH3Index: String? = null
        private var lastToken: String? = null
        private var autoReconnect = true
        private var reconnecting = false

        // Exponential backoff state
        private var reconnectAttempt = 0
        private var reconnectJob: Job? = null
        private val scope = CoroutineScope(Dispatchers.IO)

        companion object {
            private const val INITIAL_BACKOFF_MS = 2000L  // 2 seconds
            private const val MAX_BACKOFF_MS = 64000L     // 64 seconds max
            private const val MAX_RECONNECT_ATTEMPTS = 10
            private const val HTTP_CONFLICT = 409
        }

        fun connect(
            token: String,
            h3Index: String? = null,
            onEvent: (WebSocketResult) -> Unit,
        ) {
            this.lastToken = token
            this.lastH3Index = h3Index
            this.onEventCallback = onEvent
            this.reconnecting = false
            this.reconnectAttempt = 0  // Reset on fresh connect
            reconnectJob?.cancel()

            performConnect(token, h3Index, onEvent)
        }

        private fun performConnect(
            token: String,
            h3Index: String?,
            onEvent: (WebSocketResult) -> Unit,
        ) {
            val client = OkHttpClient()
            val baseUrl = debugSettingsManager.getEffectiveBaseUrl()
            val wsUrl = baseUrl
                .replace("https://", "wss://")
                .replace("http://", "ws://")
            Timber.d("🌐 WebSocket connecting to: $wsUrl (attempt ${reconnectAttempt + 1})")
            val requestBuilder = Request.Builder()
                .url("$wsUrl/api/drivers/ws")
                .addHeader("Authorization", "Bearer $token")

            // Add H3 header if provided
            h3Index?.let {
                requestBuilder.addHeader("Piper-H3-Hex", it)
                Timber.d("🗺️ Adding H3 header: $it")
            }

            val request = requestBuilder.build()

            webSocket =
                client.newWebSocket(
                    request,
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            Timber.d("✅ WebSocket connected")
                            reconnectAttempt = 0  // Reset backoff on successful connection
                            reconnecting = false
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
                            val statusCode = response?.code
                            val is409Conflict = statusCode == HTTP_CONFLICT
                            
                            if (is409Conflict) {
                                Timber.w("⚠️ WebSocket 409 Conflict: Session already exists on server. Will retry with backoff.")
                                onEvent(WebSocketResult.Failure("Session conflict - another connection is active. Retrying..."))
                            } else {
                                Timber.e("❌ WebSocket error (code=$statusCode): ${t.message}")
                                onEvent(WebSocketResult.Failure(t.message ?: "Unknown error"))
                            }
                            
                            handleReconnectWithBackoff(is409Conflict)
                        }

                        override fun onClosing(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            Timber.d("⚠️ WebSocket closing: $reason (code=$code)")
                            onEvent(WebSocketResult.Closing(reason))
                            webSocket.close(1000, null)
                        }

                        override fun onClosed(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            Timber.d("🔌 WebSocket closed: $reason (code=$code)")
                            onEvent(WebSocketResult.Closed(reason))
                            
                            // Don't reconnect on normal close (1000)
                            if (code != 1000) {
                                handleReconnectWithBackoff(wasConflict = false)
                            }
                        }
                    },
                )
        }

        /**
         * Handles reconnection with exponential backoff.
         * For 409 conflicts, we use longer initial delay since server needs time to clear session.
         */
        private fun handleReconnectWithBackoff(wasConflict: Boolean) {
            if (!autoReconnect || reconnecting || lastToken == null) return
            if (reconnectAttempt >= MAX_RECONNECT_ATTEMPTS) {
                Timber.e("❌ Max reconnect attempts ($MAX_RECONNECT_ATTEMPTS) reached. Giving up.")
                onEventCallback?.invoke(WebSocketResult.Failure("Connection failed after $MAX_RECONNECT_ATTEMPTS attempts. Please try again later."))
                return
            }

            reconnecting = true
            reconnectAttempt++

            // Calculate backoff delay with exponential increase
            // For 409 conflicts, we add extra delay since server session needs to timeout
            val baseDelay = INITIAL_BACKOFF_MS * 2.0.pow(reconnectAttempt - 1).toLong()
            val conflictBonus = if (wasConflict) 3000L else 0L  // Extra 3s for 409
            val backoffDelay = min(baseDelay + conflictBonus, MAX_BACKOFF_MS)

            Timber.d("🔁 Reconnecting in ${backoffDelay}ms (attempt $reconnectAttempt/$MAX_RECONNECT_ATTEMPTS)${if (wasConflict) " [409 Conflict]" else ""}")

            reconnectJob?.cancel()
            reconnectJob = scope.launch {
                delay(backoffDelay)
                
                if (autoReconnect && lastToken != null) {
                    reconnecting = false
                    performConnect(lastToken!!, lastH3Index, onEventCallback ?: return@launch)
                }
            }
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
            reconnectAttempt = 0
            reconnectJob?.cancel()
            webSocket?.close(1000, "Client disconnected")
            webSocket = null
            Timber.d("🔌 WebSocket manually disconnected.")
            onEventCallback?.invoke(WebSocketResult.Disconnected)
        }

        fun isConnected(): Boolean = webSocket != null

        fun enableAutoReconnect(enable: Boolean) {
            autoReconnect = enable
            if (!enable) {
                reconnectJob?.cancel()
            }
        }

        /**
         * Force reset the reconnection state. Useful if user explicitly requests reconnection.
         */
        fun resetReconnectState() {
            reconnectAttempt = 0
            reconnecting = false
            reconnectJob?.cancel()
        }
    }

