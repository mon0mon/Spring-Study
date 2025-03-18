package xyz.mon0mon.chatsample.repository.chat

import org.springframework.data.jpa.repository.JpaRepository
import xyz.mon0mon.chatsample.domain.chat.ChatRoom
import xyz.mon0mon.chatsample.domain.user.User

interface ChatRoomRepository: JpaRepository<ChatRoom, Long> {
    fun findByName(name: String): ChatRoom?

    fun findAllByParticipantsContaining(user: User): MutableList<ChatRoom>
}
