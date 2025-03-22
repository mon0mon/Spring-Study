package xyz.mon0mon.chatsample.security.socket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component
import xyz.mon0mon.chatsample.event.WebSocketCloseEvent
import xyz.mon0mon.chatsample.security.jwt.JwtSecurityException
import xyz.mon0mon.chatsample.security.jwt.JwtTokenProvider
import java.time.Instant

private val logger = KotlinLogging.logger { }

@Component
class AuthChannelInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
    private val eventPublisher: ApplicationEventPublisher
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        logger.debug { "WebSocket Interceptor" }

        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
        if (accessor?.command == StompCommand.CONNECT || accessor?.command == StompCommand.SEND) {
            val token = accessor.getFirstNativeHeader("Authorization")
                ?: throw JwtSecurityException("Authorization header is missing", HttpStatus.UNAUTHORIZED)

            if (!token.startsWith("Bearer ")) {
                throw JwtSecurityException("Authorization header is invalid", HttpStatus.UNAUTHORIZED)
            }

            val jwt = token.substring("Bearer ".length)
            logger.debug { "WebSocket Token : $jwt" }

            if (!jwtTokenProvider.validateToken(jwt)) {
                throw JwtSecurityException("Expired or invalid JWT token", HttpStatus.UNAUTHORIZED)
            }

            // JWT 토큰의 만료 시간을 Instant 형태로 추출 (jwtTokenProvider에 getExpiration 메서드가 있다고 가정)
            val expirationTime: Instant = jwtTokenProvider.getExpiration(jwt)
            eventPublisher.publishEvent(
                WebSocketCloseEvent(sessionId = accessor.sessionId!!, expireTime = expirationTime)
            )

            val authentication = jwtTokenProvider.getAuthentication(jwt)
            accessor.user = authentication
        }

        return message
    }
}

