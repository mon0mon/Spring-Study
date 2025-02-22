package xyz.mon0mon.springwebsocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// WebSocket 구성을 위한 설정 클래스임을 나타내는 어노테이션
@Configuration
// Spring WebSocket을 활성화하는 어노테이션
@EnableWebSocket
public class ServerWebSocketConfig implements WebSocketConfigurer {
    // WebSocketHandler를 등록하기 위한 메서드
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "/websocket" 경로에 webSocketHandler()에서 반환된 WebSocketHandler를 등록
        registry.addHandler(webSocketHandler(), "/websocket");
    }

    // WebSocketHandler를 Spring Bean으로 등록하는 메서드
    @Bean
    public WebSocketHandler webSocketHandler() {
        // ServerWebSocketHandler의 인스턴스를 생성하여 반환
        return new ServerWebSocketHandler();
    }
}
