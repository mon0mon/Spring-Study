package xyz.mon0mon.springwebsocketsockjs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ServerWebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 웹소켓 핸들러를 /websocket-sockjs 경로에 매핑
        registry.addHandler(webSocketHandler(), "/websocket-sockjs")
                // 모든 오리진 허용 (CORS 관련 요청에서 사용)
                .setAllowedOriginPatterns("*")
                .withSockJS()
                // 클라이언트가 WebSocket 지원 시 우선 사용
                // Load Balancer에서 WebSocket을 지원하지 않을 때는 false로 설정
                .setWebSocketEnabled(true)
                // 서버와 클라이언트 간의 연결 상태 확인을 위해 25초 간격 하트비트 전송
                .setHeartbeatTime(25000)
                // 클라이언트가 연결을 종료한 후 5초 지연 후 실제 연결 종료
                .setDisconnectDelay(5000)
                // SockJS의 iframe-based transports를 사용할 때 필요한 JS 클라이언트 라이브러리 URL을 지정
                .setClientLibraryUrl("http://localhost:8080/js/sockjs.js")
                // 핸드쉐이크 시 세션 쿠키 사용하지 않음
                .setSessionCookieNeeded(false);
    }


    @Bean
    public WebSocketHandler webSocketHandler() {
        return new ServerWebSocketHandler();
    }
}
