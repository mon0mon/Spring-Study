package xyz.mon0mon.chatsample.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import xyz.mon0mon.chatsample.event.WebSocketCloseEvent
import java.time.Duration
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

@Component
class WebSocketSessionExpirationScheduler(
    private val taskScheduler: TaskScheduler,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @EventListener(WebSocketCloseEvent::class)
    fun scheduleDisconnect(event: WebSocketCloseEvent) {
        val delay = Duration.between(Instant.now(), event.expireTime)
        if (delay.isNegative || delay.isZero) {
            disconnectSession(event.sessionId)
        } else {
            // expireTime에 맞춰 disconnect 작업을 예약합니다.
            taskScheduler.schedule({ disconnectSession(event.sessionId) }, Date.from(event.expireTime))
            logger.info { "Session ${event.sessionId} scheduled to disconnect at ${event.expireTime} (in ${delay.seconds} seconds)" }
        }
    }

    private fun disconnectSession(sessionId: String) {
        // 실제 세션 종료 로직: 예를 들어, 세션 관리 객체나 SimpUserRegistry를 이용하여 세션을 종료할 수 있습니다.
        // 아래는 예시로 로그를 남기는 형태입니다.
        logger.info { "Disconnecting session $sessionId due to JWT expiration" }
        // 예: websocketSessionManager.closeSession(sessionId)
        // 또는 messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "Session expired")
    }
}
