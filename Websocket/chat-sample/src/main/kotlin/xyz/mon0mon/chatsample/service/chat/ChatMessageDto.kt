package xyz.mon0mon.chatsample.service.chat

import xyz.mon0mon.chatsample.domain.user.User
import java.time.OffsetDateTime

class ChatMessageDto(
    val chatRoomId: Long,
    val message: String,
    val timestamp: OffsetDateTime,
    sendUser: User
) {
    val sender: String = sendUser.name
}
