package xyz.mon0mon.springwebsocket;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Sec-WebSocket-Accept 헤더의 값을 생성하는 클래스
 */
public class SecWebSocketAccept {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        //  클라이언트가 서버로 전송한 WebSocket 요청 헤더 중 하나인 Sec-WebSocket-Key의 값
        String secWebSocketKey = "7c0RT+Z1px24ypyYfnPNbw==";
        String secWebSocketAccept = Base64
                .getEncoder()
                .encodeToString(
                        MessageDigest
                                //  SHA-1 해시 알고리즘을 사용하여 해시값을 생성
                                .getInstance("SHA-1")
                                //  WebSocket 프로토콜에서 정의된 고정된 GUID 문자열
                                //  Sec-WebSocket-Key와 결합하여 해시값을 생성
                                .digest((secWebSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                        .getBytes(StandardCharsets.UTF_8)
                                )
                );

        //  Sec-WebSocket-Accept 헤더의 값
        System.out.println(secWebSocketAccept); // O1a/o0MeFzoDgn+kCKR91UkYDO4=
    }
}
