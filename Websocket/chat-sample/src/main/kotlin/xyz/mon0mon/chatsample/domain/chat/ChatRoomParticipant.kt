package xyz.mon0mon.chatsample.domain.chat

import jakarta.persistence.*
import xyz.mon0mon.chatsample.domain.support.jpa.BaseAggregateRoot
import xyz.mon0mon.chatsample.domain.user.User

@Entity
@Table(
    name = "chat_room_participant",
    uniqueConstraints = [UniqueConstraint(columnNames = ["chat_room_id", "user_id"])]
)
data class ChatRoomParticipant(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    val chatRoom: ChatRoom,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
): BaseAggregateRoot<ChatRoomParticipant>()
