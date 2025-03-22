package xyz.mon0mon.chatwebsocket.service.chat

import xyz.mon0mon.chatwebsocket.domain.chat.ChatMessage
import java.time.OffsetDateTime

class ChatMessageDto(
    chatMessage: ChatMessage
) {
    val id: Long = chatMessage.id!!
    val chatRoomId: Long = chatMessage.chatRoom.id!!
    val content: String = chatMessage.content
    val timestamp: OffsetDateTime = chatMessage.createdAt
    val sender: String = chatMessage.sender.name
}
