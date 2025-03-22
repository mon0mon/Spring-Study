package xyz.mon0mon.chatwebsocket.repository.chat

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import xyz.mon0mon.chatwebsocket.domain.chat.ChatMessage
import xyz.mon0mon.chatwebsocket.domain.chat.ChatRoom

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findAllByChatRoom(
        chatRoom: ChatRoom,
        pageable: Pageable
    ): Page<ChatMessage>
}
