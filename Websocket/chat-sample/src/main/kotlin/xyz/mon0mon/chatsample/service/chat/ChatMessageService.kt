package xyz.mon0mon.chatsample.service.chat

import org.springframework.data.domain.Page
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
) {

    fun createMessage(userId: Long, chatRoomId: Long, message: String) {
        val user = userRepository.findByIdOrThrow(userId)
        val chatRoom = chatRoomRepository.findByIdOrThrow(chatRoomId)

        if (chatRoom.isParticipant(user).not())
            throw IllegalArgumentException("User is not a participant of the chat room")

        val chatMessage = ChatMessage(chatRoom = chatRoom, sender = user, content = message)
        chatMessageRepository.save(chatMessage)

        // 예시: 메시지를 ChatMessageDto로 변환
        val chatMessageDto = ChatMessageDto(
            chatRoomId = chatRoomId, sendUserId = user.id!!, message = message, timestamp = OffsetDateTime.now()
        )

        // ChatPublisher를 이용해 roomId별로 메시지 발행
//        chatPublisher.publish(chatRoomId, chatMessageDto)
    }

    fun gets(userId: Long, chatRoomId: Long, pageable: Pageable): Page<ChatMessage> {
        val user = userRepository.findByIdOrThrow(userId)
        val chatRoom = chatRoomRepository.findByIdOrThrow(chatRoomId)

        if (chatRoom.isParticipant(user).not())
            throw IllegalArgumentException("User is not a participant of the chat room")

        return chatMessageRepository.findAllByChatRoom(chatRoom, pageable)
    }
}
