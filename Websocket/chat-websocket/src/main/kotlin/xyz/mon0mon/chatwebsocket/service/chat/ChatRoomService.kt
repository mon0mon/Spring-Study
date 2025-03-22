package xyz.mon0mon.chatwebsocket.service.chat

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.mon0mon.chatwebsocket.domain.chat.ChatRoom
import xyz.mon0mon.chatwebsocket.domain.chat.ChatRoomParticipant
import xyz.mon0mon.chatwebsocket.domain.support.extension.findByIdOrThrow
import xyz.mon0mon.chatwebsocket.repository.chat.ChatRoomParticipantRepository
import xyz.mon0mon.chatwebsocket.repository.chat.ChatRoomRepository
import xyz.mon0mon.chatwebsocket.repository.user.UserRepository

@Service
@Transactional
class ChatRoomService(
    private val userRepository: UserRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatRoomParticipantRepository: ChatRoomParticipantRepository
) {
    fun createChatRoom(
        name: String,
        userId: Long
    ) {
        val user = userRepository.findByIdOrThrow(userId)
        val chatRoom = ChatRoom(name = name, owner = user)

        chatRoomRepository.save(chatRoom)
    }

    fun get(id: Long): ChatRoom {
        return chatRoomRepository.findByIdOrThrow(id)
    }

    fun gets(userId: Long): List<ChatRoom> {
        val user = userRepository.findByIdOrThrow(userId)
        val participantList = chatRoomParticipantRepository.findByUserId(user.id!!)
        return participantList.map { it.chatRoom }
    }

    fun join(
        id: Long,
        userId: Long
    ): ChatMessageResponseDto {
        val user = userRepository.findByIdOrThrow(userId)
        val chatRoom = chatRoomRepository.findByIdOrThrow(id)

        val participant = ChatRoomParticipant(chatRoom = chatRoom, user = user)

        chatRoomParticipantRepository.save(participant)

        return ChatMessageResponseDto(success = true)
    }

    fun left(
        id: Long,
        userId: Long
    ): ChatMessageResponseDto {
        val user = userRepository.findByIdOrThrow(userId)
        val chatRoom = chatRoomRepository.findByIdOrThrow(id)

        val participant = chatRoomParticipantRepository.findByUserIdAndChatRoomId(user.id!!, chatRoom.id!!)!!

        chatRoomRepository.delete(chatRoom)

        return ChatMessageResponseDto(success = true)
    }
}
