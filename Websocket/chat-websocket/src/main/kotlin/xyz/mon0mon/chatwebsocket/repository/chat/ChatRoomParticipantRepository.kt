package xyz.mon0mon.chatwebsocket.repository.chat

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import xyz.mon0mon.chatwebsocket.domain.chat.ChatRoomParticipant

interface ChatRoomParticipantRepository : JpaRepository<ChatRoomParticipant, Long> {
    fun findByUserIdAndChatRoomId(
        userId: Long,
        chatRoomId: Long
    ): ChatRoomParticipant?

    @EntityGraph(attributePaths = ["chatRoom", "user"])
    fun findByUserId(userId: Long): List<ChatRoomParticipant>

    @Query(
        """
        select p from ChatRoomParticipant p
        where p.chatRoom.id in :chatRoomIds
    """
    )
    fun findsByChatRoomIdIn(chatRoomIds: List<Long>): List<ChatRoomParticipant>
}
