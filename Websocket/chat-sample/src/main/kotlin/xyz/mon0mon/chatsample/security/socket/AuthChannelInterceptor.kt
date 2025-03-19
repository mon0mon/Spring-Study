package xyz.mon0mon.chatsample.security.socket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessagingException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
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
        if (accessor?.command == StompCommand.CONNECT) {
            try {
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

            } catch (ex: JwtSecurityException) {
                logger.error { ex.message }

                // 에러 정보를 헤더에 담거나, 필요하면 별도의 ERROR 프레임 전송 로직 구현 가능
                accessor.setHeader("error", "Token expired. Please refresh your token.")

//                accessor.command = StompCommand.ERROR
                accessor.message = ex.message
                accessor.destination = "/user/queue/1/error"
                val message = ex.message

                logger.debug { "Publish STOMP Exception : $message" }

                val errorMessage = MessageBuilder.createMessage(message.toByteArray(), accessor.messageHeaders)

                // 예외를 던지면 클라이언트는 연결 실패 처리를 하게 됨
                throw MessagingException(errorMessage, "Token expired. Please refresh your token.")
            }
        }

        return message
    }
}

