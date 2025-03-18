package xyz.mon0mon.chatsample.controller.chat

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import xyz.mon0mon.chatsample.security.DefaultSecurityContext
import xyz.mon0mon.chatsample.service.chat.ChatMessageService
import xyz.mon0mon.chatsample.service.chat.ChatRoomService

private val logger = KotlinLogging.logger { }

@Controller
class ChatController(
    private val chatRoomService: ChatRoomService,
    private val chatMessageService: ChatMessageService
) {

    @PostMapping("/chat")
    @ResponseBody
    fun createRoom(@RequestBody req: ChatRoomCreateViewReq) {
        val userId = DefaultSecurityContext.userId()!!

        chatRoomService.createChatRoom(name = req.name, userId = userId)
    }

    @MessageMapping("/chat/{roomId}.join")
    fun joinRoom(@DestinationVariable roomId: Long) {
        val userId = DefaultSecurityContext.userId()!!

        chatRoomService.join(id = roomId, userId = userId)
    }

    @MessageMapping("/chat/{roomId}.left")
    fun leftRoom(@DestinationVariable roomId: Long) {
        val userId = DefaultSecurityContext.userId()!!

        chatRoomService.left(id = roomId, userId = userId)
    }

    @MessageMapping("/chat/{roomId}.send")
    fun sendMessage(
        @DestinationVariable roomId: Long,
        @Payload req: ChatMessageCreateViewReq
    ) {
        val userId = DefaultSecurityContext.userId()!!

        chatMessageService.createMessage(userId = userId, chatRoomId = roomId, message = req.message)
    }

    @MessageMapping("/chat/{roomId}.histroy")
    fun getMessages(@DestinationVariable roomId: Long, @Payload page: ReqPagePayload) {
        val userId = DefaultSecurityContext.userId()!!

        val pageable = PageRequest.of(page.page, page.size)

        chatMessageService.gets(userId = userId, chatRoomId = roomId, pageable = pageable)
    }
}
