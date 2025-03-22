package xyz.mon0mon.chatwebsocket.service.chat

import java.time.OffsetDateTime

data class ChatMessageResponseDto(
    val success: Boolean,
    val content: Any? = null,
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)
