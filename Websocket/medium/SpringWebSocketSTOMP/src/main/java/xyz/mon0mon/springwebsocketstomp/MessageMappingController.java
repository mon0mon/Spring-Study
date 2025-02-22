package xyz.mon0mon.springwebsocketstomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Controller
public class MessageMappingController {

    @MessageMapping("/request-without-response")
    public void handleMessageWithoutResponse(String message) {
        log.info("Message without response: {}", message);
    }

    @MessageMapping("/request")
    @SendTo("/queue/responses")
    public String handleMessageWithExplicitResponse(String message) {
        log.info("Message without response: {}", message);

        if (message.equals("zero")) {
            throw new RuntimeException(String.format("'%s' is rejected", message));
        }

        return String.format("Server response: %s", HtmlUtils.htmlEscape(message));
    }

    // /topic/request-with-implicit-response 다음 경로로 응답
    @MessageMapping("/request-with-implicit-response")
    public String handleMessageWithImplicitResponse(String message) {
        log.info("Message without response: {}", message);
        return String.format("response to " + HtmlUtils.htmlEscape(message));
    }

    @MessageExceptionHandler
    @SendTo("/queue/errors")
    public String handleException(Throwable exception) {
        log.error("Server exception", exception);
        return "server exception: " + exception.getMessage();
    }
}
