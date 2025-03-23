package xyz.mon0mon.chatsample.security.socket

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import xyz.mon0mon.chatsample.exception.handler.StompErrorMessage

@Component
class CustomClientOutboundInterceptor(
    private val objectMapper: ObjectMapper
) : ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        // 먼저 메시지 타입이 DISCONNECT_ACK 인지 확인
        val simpMessageType = message.headers["simpMessageType"] as SimpMessageType
        if (simpMessageType == SimpMessageType.DISCONNECT_ACK) {
            // 헤더 내에 포함된 simpDisconnectMessage를 추출
            val disconnectMessage = message.headers["simpDisconnectMessage"] as? Message<*>
            // nested nativeHeaders 확인
            val nativeHeaders = disconnectMessage?.headers?.get("nativeHeaders") as? Map<*, *>
            // nativeHeaders의 "message" 값은 List 형태로 전달됨
            val messages = nativeHeaders?.get("message") as? List<*>
            if (messages != null && messages.contains("Session expired")) {
                val accessor = StompHeaderAccessor.create(StompCommand.ERROR)
                accessor.message = "Session expired"

                val errorMessage = StompErrorMessage(status = HttpStatus.UNAUTHORIZED, message = "Session expired")
                val payload = objectMapper.writeValueAsBytes(errorMessage)

                accessor.setContentLength(payload.size)
                accessor.addNativeHeader("simpSessionId", message.headers["simpSessionId"] as String)
                accessor.setHeader("simpSessionId", message.headers["simpSessionId"] as String)

                return MessageBuilder.createMessage(payload, accessor.messageHeaders)
            }
        }
        return message
    }
}
