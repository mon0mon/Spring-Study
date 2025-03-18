package xyz.mon0mon.chatsample.domain.chat

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
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

    @OneToMany
    var participants: MutableList<User> = listOf(owner).toMutableList()
): BaseAggregateRoot<ChatRoom>() {
    fun addUser(user: User) {
        participants.add(user)
        userCount++
    }

    fun removeUser(user: User) {
        participants.remove(user)
        userCount--
    }

    fun isOwner(user: User): Boolean {
        return owner == user
    }

    fun isParticipant(user: User): Boolean {
        return participants.contains(user)
    }
}
