package xyz.mon0mon.chatsample.domain.chat

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.ColumnDefault
import xyz.mon0mon.chatsample.domain.support.jpa.BaseAggregateRoot
import xyz.mon0mon.chatsample.domain.user.User

@Entity
class ChatRoom(
    @Column(length = 50, nullable = false)
    var name: String,

    @ColumnDefault("0")
    @Column(nullable = false)
    var userCount: Int = 1,

    @ManyToOne(fetch = FetchType.LAZY)
    val owner: User,
): BaseAggregateRoot<ChatRoom>() {
    fun isOwner(user: User): Boolean {
        return owner == user
    }
}
