package xyz.mon0mon.chatwebsocket.repository.user

import org.springframework.data.jpa.repository.JpaRepository
import xyz.mon0mon.chatwebsocket.domain.user.User

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}
