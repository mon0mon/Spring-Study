package xyz.mon0mon.springkotlinmultimodule2

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

private val logger: Logger = LoggerFactory.getLogger(SpringKotlinMultiModule2Application::class.java)

@SpringBootApplication
class SpringKotlinMultiModule2Application {
    @Bean
    fun commandLineRunner(): CommandLineRunner {
        return CommandLineRunner {
            logger.info("Running Api Module")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<SpringKotlinMultiModule2Application>(*args)
}

@RestController
class TestController {
    @GetMapping("/test")
    fun hello(): String {
        return "hello"
    }
}
