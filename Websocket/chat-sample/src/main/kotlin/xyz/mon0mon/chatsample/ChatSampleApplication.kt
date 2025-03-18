package xyz.mon0mon.chatsample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChatSampleApplication

fun main(args: Array<String>) {
    runApplication<ChatSampleApplication>(*args)
}
