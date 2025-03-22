package xyz.mon0mon.chatwebsocket.service.chat

enum class OperationType(val value: String) {
    JOIN("join"),
    LEAVE("leave"),
    SEND("send");

    companion object {
        fun from(value: String): OperationType {
            return entries.first { it.value == value }
        }
    }
}
