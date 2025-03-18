package xyz.mon0mon.chatsample.domain.chat

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import xyz.mon0mon.chatsample.domain.support.jpa.BaseAggregateRoot
import xyz.mon0mon.chatsample.domain.user.User

@Entity
class ChatMessage(
    @ManyToOne(fetch = FetchType.LAZY)
    val sender: User,

    @Column(length = 1000, nullable = false)
    val content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val chatRoom: ChatRoom
): BaseAggregateRoot<ChatMessage>()
