package xyz.mon0mon.springwebsocketstompclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringWebSocketStompClientApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringWebSocketStompClientApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}
