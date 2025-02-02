package xyz.mon0mon.springkotlinmultimodule

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

private val logger: Logger = LoggerFactory.getLogger(SpringKotlinMultiModuleApi::class.java)

@SpringBootApplication
class SpringKotlinMultiModuleApi {
    @Bean
    fun init(): CommandLineRunner {
        return CommandLineRunner {
            logger.info(User("Spring", 23).toString())
        }
    }
}

fun main(args: Array<String>) {
    runApplication<SpringKotlinMultiModuleApi>(*args)
}

@RestController
class HelloController {
    @GetMapping("/hello")
    fun hello(): String {
        return "hello";
    }
}
