package xyz.mon0mon.chatwebsocket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChatWebsocketApplication

fun main(args: Array<String>) {
    runApplication<ChatWebsocketApplication>(*args)
}
