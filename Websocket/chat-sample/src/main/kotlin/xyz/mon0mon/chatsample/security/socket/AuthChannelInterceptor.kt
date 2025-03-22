package xyz.mon0mon.chatsample.security.socket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component
import xyz.mon0mon.chatsample.security.jwt.JwtSecurityException
import xyz.mon0mon.chatsample.security.jwt.JwtTokenProvider

private val logger = KotlinLogging.logger { }

@Component
class AuthChannelInterceptor(
    private val jwtTokenProvider: JwtTokenProvider
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

            val authentication = jwtTokenProvider.getAuthentication(jwt)
            accessor.user = authentication
        }

        return message
    }
}

