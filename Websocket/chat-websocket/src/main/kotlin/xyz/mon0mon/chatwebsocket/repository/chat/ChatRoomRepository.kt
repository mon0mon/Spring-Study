package xyz.mon0mon.chatwebsocket.repository.chat

import org.springframework.data.jpa.repository.JpaRepository
import xyz.mon0mon.chatwebsocket.domain.chat.ChatRoom

interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {
    fun findByName(name: String): ChatRoom?
}
