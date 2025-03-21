package xyz.mon0mon.chatsample.controller.chat

import org.springframework.messaging.handler.annotation.*
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import xyz.mon0mon.chatsample.domain.support.extension.findByIdOrThrow
import xyz.mon0mon.chatsample.repository.user.UserRepository
import xyz.mon0mon.chatsample.security.DefaultSecurityContext
import xyz.mon0mon.chatsample.security.jwt.JwtSecurityException
import xyz.mon0mon.chatsample.service.chat.ChatMessageService
import xyz.mon0mon.chatsample.service.chat.ChatRoomParticipantService
import xyz.mon0mon.chatsample.service.chat.ChatRoomService
import xyz.mon0mon.chatsample.service.chat.MessageType

@Controller
class ChatController(
    private val chatRoomService: ChatRoomService,
    private val chatMessageService: ChatMessageService,
    private val userRepository: UserRepository,
    private val chatRoomParticipantService: ChatRoomParticipantService
) {

    @PostMapping("/chat")
    @ResponseBody
    fun createRoom(@RequestBody req: ChatRoomCreateViewReq) {
        val userId = DefaultSecurityContext.userId()!!

        chatRoomService.createChatRoom(name = req.name, userId = userId)
    }

    @GetMapping("/rooms")
    @ResponseBody
    fun getRooms(): ChatRoomsViewRes {
        val userId = DefaultSecurityContext.userId()!!

        val chatRooms = chatRoomService.gets(userId = userId)
        val participants = chatRoomParticipantService.getParticipants(chatRoomIds = chatRooms.mapNotNull { it.id })

        return ChatRoomsViewRes(chatRooms = chatRooms, participants = participants)
    }

    /**
     * 클라이언트는 /app/chat/{roomId} 로 메시지를 전송합니다.
     * 서버는 해당 채팅방에 대해 /topic/chat/{roomId} 에 응답을 브로드캐스트합니다.
     */
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    fun processMessage(
        @DestinationVariable roomId: Long,
        @Payload reqPayload: ReqPayload,
        headerAccessor: SimpMessageHeaderAccessor
    ): Any {
        val userId = DefaultSecurityContext.userId()!!
        val user = userRepository.findByIdOrThrow(userId)

        // 메시지 내 native header "Type" 값을 읽어 어떤 작업을 할지 결정
        val type = MessageType.from(
            headerAccessor.getFirstNativeHeader("Type") ?: throw IllegalArgumentException("Type header is required"))

        return chatMessageService.processMessage(userId, roomId, type, reqPayload.content)
    }

    @MessageExceptionHandler(JwtSecurityException::class)
    fun handleJwtException(ex: JwtSecurityException): ErrorMessage {
        // 클라이언트가 이해할 수 있는 포맷의 에러 메시지 반환
        return ErrorMessage(status = ex.httpStatus.value(), message = ex.message)
    }
}

data class ErrorMessage(val status: Int, val message: String)
