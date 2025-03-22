package xyz.mon0mon.chatwebsocket.service.user

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.mon0mon.chatwebsocket.domain.support.extension.findByIdOrThrow
import xyz.mon0mon.chatwebsocket.domain.user.User
import xyz.mon0mon.chatwebsocket.repository.user.UserRepository
import xyz.mon0mon.chatwebsocket.security.AccessTokenService

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val accessTokenService: AccessTokenService,
    private val passwordEncoder: PasswordEncoder
) {
    fun login(
        email: String,
        password: String
    ): Pair<String, User> {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")

        if (!passwordEncoder.matches(password, user.password)) {
            throw IllegalArgumentException("Password is incorrect")
        }

        return accessTokenService.create(user.id!!) to user
    }

    fun register(
        email: String,
        password: String,
        name: String
    ) {
        val user = User(email = email, password = password, name = name)

        userRepository.save(user)
    }

    fun updateInfo(
        userId: Long,
        name: String,
        password: String
    ): User {
        val user = userRepository.findByIdOrThrow(userId)

        user.update(name = name, password = password)

        return userRepository.save(user)
    }

    fun get(userId: Long): User {
        return userRepository.findByIdOrThrow(userId)
    }
}
