package xyz.mon0mon.chatwebsocket.controller.chat

import xyz.mon0mon.chatwebsocket.domain.chat.ChatRoom
import xyz.mon0mon.chatwebsocket.domain.chat.ChatRoomParticipant

class ChatRoomsViewRes (
    chatRooms: List<ChatRoom>,
    participants: Map<Long, List<ChatRoomParticipant>>
) {
    val chatRooms = chatRooms.map { ChatRoomData(it, participants[it.id]!!) }
}

class ChatRoomData(
    chatRoom: ChatRoom, participants: List<ChatRoomParticipant>
) {
    val id = chatRoom.id
    val name = chatRoom.name
    val participants = participants.map { ChatRoomParticipantData(it) }
    val owner = chatRoom.owner.name
}

class ChatRoomParticipantData(
    participant: ChatRoomParticipant
) {
    val name = participant.user.name
}
