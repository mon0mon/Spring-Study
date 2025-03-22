package xyz.mon0mon.chatsample.exception.handler

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.messaging.Message
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler
import xyz.mon0mon.chatsample.security.jwt.JwtSecurityException

@Component
class StompExceptionHandler(
    private val objectMapper: ObjectMapper
) : StompSubProtocolErrorHandler() {
    override fun handleClientMessageProcessingError(
        clientMessage: Message<ByteArray>?, ex: Throwable
    ): Message<ByteArray>? {
        val accessor = StompHeaderAccessor.create(StompCommand.ERROR)
        accessor.message = ex.message
        accessor.setLeaveMutable(true)

        if (ex is MessageDeliveryException) {
            if (ex.cause is JwtSecurityException) {
                val caused = ex.cause as JwtSecurityException
                val errorMessage = StompErrorMessage(status = caused.httpStatus, message = caused.message)
                val payload = objectMapper.writeValueAsBytes(errorMessage)

                accessor.setContentLength(payload.size)
                accessor.message = caused.message

                return MessageBuilder.createMessage(payload, accessor.messageHeaders)
            }
        }

        return super.handleClientMessageProcessingError(clientMessage, ex)
    }
}
