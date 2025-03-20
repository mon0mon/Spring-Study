package xyz.mon0mon.chatsample.service.chat

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.mon0mon.chatsample.domain.chat.ChatMessage
import xyz.mon0mon.chatsample.domain.support.extension.findByIdOrThrow
import xyz.mon0mon.chatsample.repository.chat.ChatMessageRepository
import xyz.mon0mon.chatsample.repository.chat.ChatRoomRepository
import xyz.mon0mon.chatsample.repository.user.UserRepository
import java.time.OffsetDateTime

@Service
@Transactional
class ChatMessageService(
    private val userRepository: UserRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatRoomService: ChatRoomService
) {

    fun processMessage(userId: Long, roomId: Long, type: MessageType, content: String? = null, size: Int = 0, page: Int = 0): Any {
        return when (type) {
            MessageType.SEND -> {
                createMessage(userId, roomId, content!!)
            }
            MessageType.HISTORY -> {
                gets(userId, roomId, PageRequest.of(page, size))
            }
            MessageType.JOIN -> {
                chatRoomService.join(userId, roomId)
            }
            MessageType.LEAVE -> {
                chatRoomService.left(userId, roomId)
            }
        }
    }

    fun createMessage(userId: Long, chatRoomId: Long, message: String): ChatMessageDto {
        val user = userRepository.findByIdOrThrow(userId)
        val chatRoom = chatRoomRepository.findByIdOrThrow(chatRoomId)

        if (chatRoom.isParticipant(user).not())
            throw IllegalArgumentException("User is not a participant of the chat room")

        val chatMessage = ChatMessage(chatRoom = chatRoom, sender = user, content = message)
        chatMessageRepository.save(chatMessage)

        // 예시: 메시지를 ChatMessageDto로 변환
        return ChatMessageDto(
            chatRoomId = chatRoomId, sendUser = user, message = message, timestamp = OffsetDateTime.now()
        )
    }

    fun gets(userId: Long, chatRoomId: Long, pageable: Pageable): Page<ChatMessageDto> {
        val user = userRepository.findByIdOrThrow(userId)
        val chatRoom = chatRoomRepository.findByIdOrThrow(chatRoomId)

        if (chatRoom.isParticipant(user).not())
            throw IllegalArgumentException("User is not a participant of the chat room")

        val chatMessages = chatMessageRepository.findAllByChatRoom(chatRoom, pageable)

        // 예시: ChatMessage를 ChatMessageDto로 변환
        return chatMessages.map { ChatMessageDto(chatRoomId, it.content, it.createdAt, it.sender) }
    }
}
