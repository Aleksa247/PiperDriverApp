package com.piperrideshare.driver.api

import com.piperrideshare.driver.BuildConfig
import com.piperrideshare.driver.data.network.WebSocketResult
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

    private var onEventCallback: ((WebSocketResult) -> Unit)? = null

    fun connect(
        token: String,
        onEvent: (WebSocketResult) -> Unit,
    ) {
        onEventCallback = onEvent

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("wss://${BuildConfig.BASE_URL}/api/drivers/ws")
            .addHeader("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    println("✅ WebSocket connected")
                    onEvent(WebSocketResult.Connected)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    println("📨 WebSocket message received: $text")
                    onEvent(WebSocketResult.Message(text))
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    val text = bytes.utf8()
                    println("📨 WebSocket binary message received: $text")
                    onEvent(WebSocketResult.Message(text))
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    println("❌ WebSocket error: ${t.message}")
                    onEvent(WebSocketResult.Failure(t.message ?: "Unknown error"))
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    println("⚠️ WebSocket closing: $reason")
                    onEvent(WebSocketResult.Closing(reason))
                    webSocket.close(1000, null)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    println("🔌 WebSocket closed: $reason")
                    onEvent(WebSocketResult.Closed(reason))
                }
            }
        )
    }

    fun send(message: String) {
        if (webSocket != null) {
            println("📤 Sending message: $message")
            webSocket?.send(message)
        } else {
            println("⚠️ WebSocket is not connected. Cannot send message.")
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
        println("🔌 WebSocket manually disconnected.")
        onEventCallback?.invoke(WebSocketResult.Disconnected)
    }

    private fun onEvent(result: WebSocketResult) {
        onEventCallback?.invoke(result)
    }
}
