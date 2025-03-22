package xyz.mon0mon.chatsample.event

import java.time.Instant

data class WebSocketCloseEvent(
    val sessionId: String,
    val expireTime: Instant
)
