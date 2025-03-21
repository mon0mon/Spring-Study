package xyz.mon0mon.chatsample

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.crypto.password.PasswordEncoder
import xyz.mon0mon.chatsample.domain.chat.ChatRoom
import xyz.mon0mon.chatsample.domain.chat.ChatRoomParticipant
import xyz.mon0mon.chatsample.domain.user.User
import xyz.mon0mon.chatsample.repository.chat.ChatRoomParticipantRepository
import xyz.mon0mon.chatsample.repository.chat.ChatRoomRepository
import xyz.mon0mon.chatsample.repository.user.UserRepository

private val logger = KotlinLogging.logger { }

@SpringBootApplication
@EnableScheduling
class ChatSampleApplication {

    @Bean
    fun init(
        userRepository: UserRepository, chatRoomRepository: ChatRoomRepository,
        chatRoomParticipantRepository: ChatRoomParticipantRepository, passwordEncoder: PasswordEncoder
    ) =
        CommandLineRunner {
            try {
                val user1 = userRepository.save(
                    User(name = "user1", email = "user1@example.com", password = passwordEncoder.encode("1234")))
                val user2 = userRepository.save(
                    User(name = "user2", email = "user2@example.com", password = passwordEncoder.encode("1234")))

                val chatRoom1 = chatRoomRepository.save(ChatRoom(name = "chatroom-1", owner = user1))
                val chatRoom2 = chatRoomRepository.save(ChatRoom(name = "chatroom-2", owner = user2))

                val chatRoom1Participant1 = chatRoomParticipantRepository.save(ChatRoomParticipant(chatRoom1, user1))
                val chatRoom1Participant2 = chatRoomParticipantRepository.save(ChatRoomParticipant(chatRoom1, user2))

                val chatRoom2Participant1 = chatRoomParticipantRepository.save(ChatRoomParticipant(chatRoom2, user1))
                val chatRoom2Participant2 = chatRoomParticipantRepository.save(ChatRoomParticipant(chatRoom2, user2))
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
