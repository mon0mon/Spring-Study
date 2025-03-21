package xyz.mon0mon.chatsample.controller.chat

import xyz.mon0mon.chatsample.service.chat.MessageType

data class ReqPayload(
    val type: MessageType,
    val content: String? = null
)
