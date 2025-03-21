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

    // 10초마다 모든 채널에 메시지를 보냅니다.
//    @Scheduled(fixedRate = 10_000)
    @Scheduled(fixedRate = 2_000)
    fun sendScheduledMessage() {

        logger.debug { "Broadcasting Scheduled Message to All Channels" }

        val chatRooms = chatRoomRepository.findAll()

        val scheduledChatMessage = ScheduledChatMessage()
        messageSendingOperations.convertAndSend("/topic/periodic", scheduledChatMessage)
    }
}

data class ScheduledChatMessage(
    val roomId: Long? = null,
    val message: String = "server periodic message",
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)
