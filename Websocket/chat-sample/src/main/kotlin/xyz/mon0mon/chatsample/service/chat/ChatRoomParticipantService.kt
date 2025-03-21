package xyz.mon0mon.chatsample.service.chat

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.mon0mon.chatsample.domain.chat.ChatRoomParticipant
import xyz.mon0mon.chatsample.repository.chat.ChatRoomParticipantRepository

@Service
@Transactional
class ChatRoomParticipantService(
    private val chatRoomParticipantRepository: ChatRoomParticipantRepository
) {
    fun getParticipants(chatRoomIds: List<Long>): Map<Long, List<ChatRoomParticipant>> {
        val participants = chatRoomParticipantRepository.findsByChatRoomIdIn(chatRoomIds)

        return participants.groupBy { it.chatRoom.id!! }
    }
}
