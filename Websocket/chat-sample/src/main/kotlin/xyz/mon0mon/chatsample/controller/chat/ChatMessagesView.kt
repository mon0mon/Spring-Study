package xyz.mon0mon.chatsample.controller.chat

import org.springframework.data.domain.Page
import xyz.mon0mon.chatsample.service.chat.ChatMessageDto

class ChatMessagesViewRes (
    page: Page<ChatMessageDto>
) {
    val totalElements: Long = page.totalElements
    val totalPages: Int = page.totalPages
    val currentPage: Int = page.number
    val pageSize: Int = page.size
    val first: Boolean = page.isFirst
    val last: Boolean = page.isLast
    val messages: List<ChatMessageData> = page.content.map { ChatMessageData(it) }
}

class ChatMessageData (
    dto: ChatMessageDto
) {
    val id: Long = dto.id
    val chatRoomId: Long = dto.chatRoomId
    val sender: String = dto.sender
    val content: String = dto.content
    val timestamp: String = dto.timestamp.toString()
}
