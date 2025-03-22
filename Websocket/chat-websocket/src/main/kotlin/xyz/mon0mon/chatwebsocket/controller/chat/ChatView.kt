package xyz.mon0mon.chatwebsocket.controller.chat

import xyz.mon0mon.chatwebsocket.service.chat.MessageType

data class ReqPayload(
    val type: MessageType,
    val content: String? = null
)
