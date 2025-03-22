package xyz.mon0mon.chatwebsocket.service.chat

enum class MessageType(val value: String) {
    JOIN("join"),
    LEAVE("leave"),
    SEND("send");

    companion object {
        fun from(value: String): MessageType {
            return entries.first { it.value == value }
        }
    }
}
