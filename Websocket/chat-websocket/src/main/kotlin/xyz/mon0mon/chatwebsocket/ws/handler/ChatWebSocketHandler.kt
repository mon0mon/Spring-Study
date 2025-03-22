package xyz.mon0mon.chatwebsocket.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import xyz.mon0mon.chatwebsocket.service.chat.ChatMessageResponseDto
import xyz.mon0mon.chatwebsocket.service.chat.ChatMessageService
import xyz.mon0mon.chatwebsocket.service.chat.ChatRoomService
import xyz.mon0mon.chatwebsocket.service.chat.MessageType
import xyz.mon0mon.chatwebsocket.service.chat.OperationType
import java.time.OffsetDateTime
import java.util.concurrent.CopyOnWriteArraySet

private val logger = KotlinLogging.logger { }

@Component
class ChatWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val chatMessageService: ChatMessageService,
    private val chatRoomService: ChatRoomService
) : TextWebSocketHandler() {
    //  WebSocket 세션들을 저장하는 Set (Thread-safe)
    private val sessions = CopyOnWriteArraySet<WebSocketSession>()

    // WebSocket 연결이 성공적으로 수립된 후 호출되는 메서드
    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info { "Server connection opened" }
        sessions.add(session)

        val message = TextMessage("one-time message from server")
        logger.info { "Server sends: $message" }

        // 세션을 통해 메시지 전송
        session.sendMessage(message)
    }

    // WebSocket 통신 중 에러 발생 시 호출되는 메서드
    override fun handleTransportError(
        session: WebSocketSession,
        exception: Throwable
    ) {
        logger.info { "Server transport error: ${exception.message}" }
        super.handleTransportError(session, exception)
    }

    // WebSocket 연결이 종료되었을 때 호출되는 메서드
    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus
    ) {
        logger.info { "Server connection closed: $status" }
        // 세션 Set에서 종료된 세션 제거
        sessions.remove(session)

        super.afterConnectionClosed(session, status)
    }

    // 클라이언트로부터 텍스트 메시지를 수신했을 때 호출되는 메서드
    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage
    ) {
        val req = objectMapper.readValue(message.payload, ChatPayloadReq::class.java)
//        val userId = DefaultSecurityContext.userId()!!
        val userId = 1L

        logger.info { "Server received: $req" }

        val res =
            when (req.operationType) {
                OperationType.SEND -> {
                    chatMessageService.createMessage(
                        userId = userId,
                        chatRoomId = req.roomId,
                        content = req.content!!,
                        messageType = req.messageType
                    )
                }

                OperationType.JOIN -> {
                    chatRoomService.join(id = req.roomId, userId = userId)
                }

                OperationType.LEAVE -> {
                    chatRoomService.left(id = req.roomId, userId = userId)
                }
            }

        publishMessage(ChatPayloadRes(res))
    }

    private fun publishMessage(message: ChatPayloadRes) {
        logger.info { "Server send to Clients" }
        sessions.filter { it.isOpen }
            .forEach { it.sendMessage(TextMessage(objectMapper.writeValueAsBytes(message))) }
    }
}

data class ChatPayloadReq(
    val operationType: OperationType,
    val messageType: MessageType,
    val content: String? = null,
    val roomId: Long
)

class ChatPayloadRes(
    resDto: ChatMessageResponseDto
) {
    val success: Boolean = true
    val content: Any? = resDto.content
    val timestamp: OffsetDateTime = resDto.timestamp
}
