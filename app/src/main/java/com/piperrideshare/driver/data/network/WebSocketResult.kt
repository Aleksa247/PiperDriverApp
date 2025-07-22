package com.piperrideshare.driver.data.network

sealed class WebSocketResult {
    object Connected : WebSocketResult()

    data class Message(
        val data: String,
    ) : WebSocketResult()

    data class Failure(
        val error: String,
    ) : WebSocketResult()

    data class Closed(
        val reason: String,
    ) : WebSocketResult()

    data class Closing(
        val reason: String,
    ) : WebSocketResult()

    object Disconnected : WebSocketResult()
}
