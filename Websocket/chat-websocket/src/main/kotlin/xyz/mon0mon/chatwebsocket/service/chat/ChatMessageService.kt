package xyz.mon0mon.chatwebsocket.service.chat

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.mon0mon.chatwebsocket.domain.chat.ChatMessage
import xyz.mon0mon.chatwebsocket.domain.support.extension.findByIdOrThrow
import xyz.mon0mon.chatwebsocket.repository.chat.ChatMessageRepository
import xyz.mon0mon.chatwebsocket.repository.chat.ChatRoomParticipantRepository
import xyz.mon0mon.chatwebsocket.repository.chat.ChatRoomRepository
import xyz.mon0mon.chatwebsocket.repository.user.UserRepository

@Service
@Transactional
class ChatMessageService(
    private val userRepository: UserRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatRoomParticipantRepository: ChatRoomParticipantRepository
) {
    fun createMessage(
        userId: Long,
        chatRoomId: Long,
        content: String,
        messageType: MessageType
    ): ChatMessageResponseDto {
        val user = userRepository.findByIdOrThrow(userId)
        val chatRoom = chatRoomRepository.findByIdOrThrow(chatRoomId)

        val participant =
            chatRoomParticipantRepository.findByUserIdAndChatRoomId(user.id!!, chatRoom.id!!)!!
                ?: throw IllegalArgumentException("User is not a participant of the chat room")

        val chatMessage = ChatMessage(chatRoom = chatRoom, sender = user, content = content)
        chatMessageRepository.save(chatMessage)

        // 예시: 메시지를 ChatMessageDto로 변환
        return ChatMessageResponseDto(success = true, content = ChatMessageDto(chatMessage), timestamp = chatMessage.createdAt)
    }

    fun gets(
        userId: Long,
        chatRoomId: Long,
        pageable: Pageable
    ): Page<ChatMessageDto> {
        val user = userRepository.findByIdOrThrow(userId)
        val chatRoom = chatRoomRepository.findByIdOrThrow(chatRoomId)

        val participant =
            chatRoomParticipantRepository.findByUserIdAndChatRoomId(user.id!!, chatRoom.id!!)!!
                ?: throw IllegalArgumentException("User is not a participant of the chat room")

        val chatMessages = chatMessageRepository.findAllByChatRoom(chatRoom, pageable)

        // 예시: ChatMessage를 ChatMessageDto로 변환
        return chatMessages.map { ChatMessageDto(it) }
    }
}
