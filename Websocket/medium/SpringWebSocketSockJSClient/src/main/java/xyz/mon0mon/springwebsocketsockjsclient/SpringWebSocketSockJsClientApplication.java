package xyz.mon0mon.springwebsocketsockjsclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringWebSocketSockJsClientApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringWebSocketSockJsClientApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}
