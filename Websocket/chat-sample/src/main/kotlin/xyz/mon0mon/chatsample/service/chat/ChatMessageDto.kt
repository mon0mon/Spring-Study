package xyz.mon0mon.chatsample.service.chat

import java.time.OffsetDateTime

data class ChatMessageDto(
    val chatRoomId: Long,
    val sendUserId: Long,
    val message: String,
    val timestamp: OffsetDateTime
)
