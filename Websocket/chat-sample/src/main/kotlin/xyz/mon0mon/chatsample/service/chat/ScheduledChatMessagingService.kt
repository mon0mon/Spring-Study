package xyz.mon0mon.chatsample.service.chat

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import xyz.mon0mon.chatsample.repository.chat.ChatRoomRepository
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger { }

@Component
class ScheduledChatMessagingService(
    private val messageSendingOperations: MessageSendingOperations<String>,
    private val chatRoomRepository: ChatRoomRepository
) {

    @Scheduled(fixedRate = 10_000)
    fun sendScheduledMessage() {

        logger.debug { "Broadcasting Scheduled Message to All Channels" }

        val chatRooms = chatRoomRepository.findAll()

        chatRooms.forEach {
            val scheduledChatMessage = ScheduledChatMessage(roomId = it.id!!)
            messageSendingOperations.convertAndSend("/topic/periodic", scheduledChatMessage)
            messageSendingOperations.convertAndSend("/topic/chat/${it.id}", scheduledChatMessage)
        }
    }
}

data class ScheduledChatMessage(
    val roomId: Long,
    val message: String = "server periodic message",
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)
