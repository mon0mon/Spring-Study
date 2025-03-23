package xyz.mon0mon.chatsample.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.DefaultContentTypeResolver
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.util.MimeTypeUtils
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import xyz.mon0mon.chatsample.exception.handler.StompExceptionHandler

@Configuration
@EnableWebSocketMessageBroker
class StompWebSocketConfig(
    private val authClientInboundInterceptor: ChannelInterceptor,
    private val customClientOutboundInterceptor: ChannelInterceptor,
    private val stompExceptionHandler: StompExceptionHandler
) : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws/stomp")
            .setAllowedOrigins("*")
        registry.setErrorHandler(stompExceptionHandler)
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic", "/queue", "/user")
        registry.setApplicationDestinationPrefixes("/app")
        registry.setUserDestinationPrefix("/user")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(authClientInboundInterceptor)
    }

    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration.interceptors(customClientOutboundInterceptor)
    }

    override fun configureMessageConverters(messageConverters: MutableList<MessageConverter?>): Boolean {
        val resolver = DefaultContentTypeResolver()
        resolver.defaultMimeType = MimeTypeUtils.APPLICATION_JSON

        val converter = MappingJackson2MessageConverter()
        converter.objectMapper = ObjectMapper()
        converter.contentTypeResolver = resolver

        messageConverters.add(converter)

        // 기본 제공되는 컨버터들을 사용하지 않음
        return false
    }
}
