package xyz.mon0mon.springwebsocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class ServerWebSocketHandler extends TextWebSocketHandler implements SubProtocolCapable {

    // Spring WebSocket event handlers들은 broadcasting을 지원하지 않음
    // WebSocket 세션들을 저장하는 Set (Thread-safe)
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    // WebSocket 연결이 성공적으로 수립된 후 호출되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Server connection opened");
        sessions.add(session);

        TextMessage message = new TextMessage("one-time message from server");
        log.info("Server sends: {}", message);
        // 세션을 통해 메시지 전송
        session.sendMessage(message);
    }

    // 클라이언트로부터 텍스트 메시지를 수신했을 때 호출되는 메서드
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String request = message.getPayload();
        log.info("Server received: {}", request);

        // 받은 메시지를 HTML Escape 처리 후 응답 메시지 생성
        String response = String.format("response from server to '%s'", HtmlUtils.htmlEscape(request));
        log.info("Server sends: {}", response);
        // 클라이언트로 응답 메시지 전송
        session.sendMessage(new TextMessage(response));
    }

    // WebSocket 통신 중 에러 발생 시 호출되는 메서드
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("Server transport error: {}", exception.getMessage());
    }

    // WebSocket 연결이 종료되었을 때 호출되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Server connection closed: {}", status);
        // 세션 Set에서 종료된 세션 제거
        sessions.remove(session);
    }

    // 지원하는 SubProtocol 목록 반환
    @Override
    public List<String> getSubProtocols() {
        return Collections.singletonList("subprotocol.demo.websocket");
    }

    // 연결된 모든 클라이언트에 메세지를 Broadcasting하는 메서드 (10초마다 실행)
    @Scheduled(fixedRate = 10000)
    void sendPeriodicMessages() throws IOException {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                String broadcast = String.format("Server periodic message %s", LocalTime.now());
                log.info("Server sends: {}", broadcast);
                session.sendMessage(new TextMessage(broadcast));
            }
        }
    }
}
