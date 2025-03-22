package xyz.mon0mon.chatwebsocket.controller.chat

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import xyz.mon0mon.chatwebsocket.repository.user.UserRepository
import xyz.mon0mon.chatwebsocket.security.DefaultSecurityContext
import xyz.mon0mon.chatwebsocket.service.chat.ChatMessageService
import xyz.mon0mon.chatwebsocket.service.chat.ChatRoomParticipantService
import xyz.mon0mon.chatwebsocket.service.chat.ChatRoomService

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

    @GetMapping("/chat/{roomId}/history")
    @ResponseBody
    fun getChatMessages(
        @PathVariable roomId: Long, @PageableDefault(size = 20, page = 0) pageable: Pageable
    ): ChatMessagesViewRes {
        val userId = DefaultSecurityContext.userId()!!

        // 채팅방(roomId)에 해당하는 메시지를 조회하는 서비스 호출
        val page = chatMessageService.gets(userId = userId, chatRoomId = roomId, pageable = pageable)

        return ChatMessagesViewRes(page = page)
    }
}

data class ErrorMessage(val status: Int, val message: String)
