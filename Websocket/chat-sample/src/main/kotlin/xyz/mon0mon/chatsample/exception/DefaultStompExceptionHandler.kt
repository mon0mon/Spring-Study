package xyz.mon0mon.chatsample.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.Message
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler
import xyz.mon0mon.chatsample.security.jwt.JwtSecurityException

private val logger = KotlinLogging.logger { }

@Component
class StompExceptionHandler : StompSubProtocolErrorHandler() {

    override fun handleInternal(
        errorHeaderAccessor: StompHeaderAccessor, errorPayload: ByteArray, cause: Throwable?,
        clientHeaderAccessor: StompHeaderAccessor?
    ): Message<ByteArray> {

        when (cause) {
            is JwtSecurityException -> {
                errorHeaderAccessor.message = cause.message
                errorHeaderAccessor.destination = "/user/queue/1/error"
                val message = cause.message

                logger.debug { "Publish STOMP Exception : $message" }

                return MessageBuilder.createMessage(message.toByteArray(), errorHeaderAccessor.messageHeaders)
            }
        }

        return super.handleInternal(errorHeaderAccessor, errorPayload, cause, clientHeaderAccessor)
    }
}
