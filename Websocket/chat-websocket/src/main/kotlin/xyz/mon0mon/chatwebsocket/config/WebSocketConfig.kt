package xyz.mon0mon.chatwebsocket.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import xyz.mon0mon.chatwebsocket.ws.handler.ChatWebSocketHandler
import xyz.mon0mon.chatwebsocket.ws.handler.PeriodicWebSocketHandler

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    private lateinit var chatHandler: ChatWebSocketHandler
    private lateinit var periodicHandler: PeriodicWebSocketHandler

    @Autowired
    fun setChatHandler(chatHandler: ChatWebSocketHandler) {
        this.chatHandler = chatHandler
    }

    @Autowired
    fun setPeriodicHandler(periodicHandler: PeriodicWebSocketHandler) {
        this.periodicHandler = periodicHandler
    }

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatHandler, "/ws/chat")
        registry.addHandler(periodicHandler, "/ws/periodic")
    }
}
