package xyz.mon0mon.springwebsocketstomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class SubscribeMappingController {

    @SubscribeMapping("/subscribe")
    public String sendOneTimeMessage() {
        log.info("Subscription via the application");
        return "server one-time message via the application";
    }
}
