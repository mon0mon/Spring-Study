package xyz.mon0mon.springwebsocketstomp;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class ScheduledController {

    private final MessageSendingOperations<String> messageSendingOperations;

    @Scheduled(fixedRate = 10_000)
    public void sendPeriodicMessages() {
        String broadcast = String.format("server periodic message %s via the broker", LocalTime.now());

        this.messageSendingOperations.convertAndSend("/topic/periodic", broadcast);
    }
}
