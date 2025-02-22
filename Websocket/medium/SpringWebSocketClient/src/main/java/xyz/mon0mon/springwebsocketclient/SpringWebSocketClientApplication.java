package xyz.mon0mon.springwebsocketclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringWebSocketClientApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringWebSocketClientApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}
