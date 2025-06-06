package xyz.mon0mon.chatwebsocket.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import xyz.mon0mon.chatwebsocket.security.DefaultSecurityContext
import java.time.OffsetDateTime
import java.util.concurrent.CopyOnWriteArraySet

private val logger = KotlinLogging.logger { }

@Component
class PeriodicWebSocketHandler(
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {
    private val sessions = CopyOnWriteArraySet<WebSocketSession>()

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus
    ) {
        sessions.remove(session)
        super.afterConnectionClosed(session, status)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info { "connected user : ${DefaultSecurityContext.user()}" }

        sessions.add(session)
        super.afterConnectionEstablished(session)
    }

    //  2초마다 모든 세션에 메시지 발송
    @Scheduled(fixedRate = 2_000)
    fun sendPeriodicMessages() {
        logger.debug { "Start sending Periodic Message to Clients" }
        sessions.filter { it.isOpen }
            .forEach {
                val broadcast = ScheduledChatMessage()
                it.sendMessage(TextMessage(objectMapper.writeValueAsBytes(broadcast)))
            }
        logger.debug { "Finish sending Periodic Message to Clients" }
    }
}

data class ScheduledChatMessage(
    val message: String = "server periodic message",
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)
