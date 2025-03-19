package xyz.mon0mon.chatsample.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import xyz.mon0mon.chatsample.exception.StompExceptionHandler

@Configuration
@EnableWebSocketMessageBroker
class StompWebSocketConfig(
    private val authChannelInterceptor: ChannelInterceptor,
    private val stompExceptionHandler: StompExceptionHandler
) : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws/stomp")
            .setAllowedOrigins("*")

        registry.setErrorHandler(stompExceptionHandler)
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic", "/queue")
        registry.setApplicationDestinationPrefixes("/app")
        registry.setUserDestinationPrefix("/user")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(authChannelInterceptor)
    }
}
