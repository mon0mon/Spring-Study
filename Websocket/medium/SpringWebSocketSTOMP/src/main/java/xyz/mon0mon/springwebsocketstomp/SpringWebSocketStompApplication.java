package xyz.mon0mon.springwebsocketstomp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringWebSocketStompApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringWebSocketStompApplication.class, args);
    }

}
