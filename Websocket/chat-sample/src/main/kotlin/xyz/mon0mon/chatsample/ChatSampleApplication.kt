package xyz.mon0mon.chatsample

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.crypto.password.PasswordEncoder
import xyz.mon0mon.chatsample.domain.chat.ChatRoom
import xyz.mon0mon.chatsample.domain.user.User
import xyz.mon0mon.chatsample.repository.chat.ChatRoomRepository
import xyz.mon0mon.chatsample.repository.user.UserRepository

private val logger = KotlinLogging.logger { }

@SpringBootApplication
@EnableScheduling
class ChatSampleApplication {

    @Bean
    fun init(userRepository: UserRepository, chatRoomRepository: ChatRoomRepository, passwordEncoder: PasswordEncoder) =
        CommandLineRunner {
            val user1 = User(name = "user1", email = "user1@example.com", password = passwordEncoder.encode("1234"))
            val user2 = User(name = "user2", email = "user2@example.com", password = passwordEncoder.encode("1234"))

            val chatRoom1 = ChatRoom(name = "chatroom-1", owner = user1)
            val chatRoom2 = ChatRoom(name = "chatroom-2", owner = user2)

            try {
                userRepository.save(user1)
                userRepository.save(user2)

                chatRoomRepository.save(chatRoom1)
                chatRoomRepository.save(chatRoom2)
            } catch (e: Exception) {
                logger.info { "Already exists" }
            } finally {
                logger.info { "Finish Inserting Data" }
            }
        }
}

fun main(args: Array<String>) {
    runApplication<ChatSampleApplication>(*args)
}
