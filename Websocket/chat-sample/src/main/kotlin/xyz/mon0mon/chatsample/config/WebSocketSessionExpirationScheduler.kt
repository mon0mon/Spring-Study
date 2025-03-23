package xyz.mon0mon.chatsample.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.simp.user.SimpUser
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.messaging.support.MessageBuilder
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.messaging.DefaultSimpUserRegistry
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import xyz.mon0mon.chatsample.event.WebSocketCloseEvent
import xyz.mon0mon.chatsample.exception.handler.StompErrorMessage
import java.time.Duration
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

private const val SESSION_EXPIRED = "Session expired"

@Component
class WebSocketSessionExpirationScheduler(
    private val taskScheduler: TaskScheduler,
    private val messagingTemplate: SimpMessagingTemplate,
    private val simpUserRegistry: SimpUserRegistry,
    private val simpleBrokerMessageHandler: AbstractBrokerMessageHandler,
    private val objectMapper: ObjectMapper
) {

    @EventListener(WebSocketCloseEvent::class)
    fun scheduleDisconnect(event: WebSocketCloseEvent) {
        val expireTime = event.expireTime
        val delay = Duration.between(Instant.now(), expireTime)
        if (delay.isNegative || delay.isZero) {
            disconnectSession(event.sessionId)
        } else {
            // expireTime에 맞춰 disconnect 작업을 예약합니다.
            taskScheduler.schedule({ disconnectSession(event.sessionId) }, Date.from(expireTime))
            logger.info { "Session ${event.sessionId} scheduled to disconnect at $expireTime (in ${delay.seconds} seconds)" }
        }
    }

    private fun disconnectSession(sessionId: String) {
        logger.info { "Disconnecting session $sessionId due to JWT expiration" }

        // 해당 세션에 해당하는 사용자의 이름을 찾습니다.
        val user = simpUserRegistry.users.firstOrNull() { user ->
            user.sessions.any { session -> session.id == sessionId }
        }

        if (user == null) {
            logger.warn { "User not found for session $sessionId" }
            return
        }

        val payloadByte = objectMapper.writeValueAsBytes(StompErrorMessage("SESSION EXPIRED", HttpStatus.UNAUTHORIZED))
        val payloadString =
            objectMapper.writeValueAsString(StompErrorMessage("SESSION EXPIRED", HttpStatus.UNAUTHORIZED))

        // 사용자 정보를 저장하는 SimpUserRegistry에서 해당 세션을 제거
        closeSimpUserRegistrySession(sessionId, payloadByte, user)
        // 구독 정보를 저장하는 SimpleBrokerMessageHandler에서 해당 세션을 제거
        closeSimpBrokerSession(sessionId, payloadByte)

        // 사용자의 "/user/{userId}/queue/errors" 구독 채널로 에러 메시지를 전송
        messagingTemplate.convertAndSendToUser(user.name, "/queue/errors", payloadString)
        logger.info { "Sent error frame to user ${user.name} for session $sessionId" }
    }

    private fun closeSimpBrokerSession(sessionId: String, payloadByte: ByteArray) {
        // SimpleBrokerMessageHandler에서 해당 세션을 제거
        val disconnectMessage = stompMessage(sessionId, payloadByte, StompCommand.DISCONNECT)
        simpleBrokerMessageHandler.handleMessage(disconnectMessage)
    }

    private fun closeSimpUserRegistrySession(
        sessionId: String, payloadByte: ByteArray, user: SimpUser
    ) {
        val userRegistry = simpUserRegistry as DefaultSimpUserRegistry
        val disconnectMessage = stompMessage(sessionId, payloadByte, StompCommand.DISCONNECT)

        // SessionDisconnectEvent 발행해서, SimpUserRegistry에서 해당 세션을 제거
        userRegistry.onApplicationEvent(
            SessionDisconnectEvent(this, disconnectMessage, sessionId, CloseStatus.NORMAL, user.principal))
        logger.info { "Session $sessionId removed from SimpUserRegistry" }
    }

    private fun stompMessage(sessionId: String, payload: ByteArray, command: StompCommand): Message<ByteArray> {
        val accessor = StompHeaderAccessor.create(command)
        accessor.message = SESSION_EXPIRED
        accessor.setContentLength(payload.size)
        accessor.sessionId = sessionId
        accessor.setLeaveMutable(true)
        return MessageBuilder.createMessage(payload, accessor.messageHeaders)
    }
}
