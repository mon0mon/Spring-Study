package xyz.mon0mon.springkotlinmultimodule2

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

private val logger: Logger = LoggerFactory.getLogger(SpringKotlinMultiModule2Application::class.java)

@SpringBootApplication
@EnableScheduling
class SpringKotlinMultiModule2Application {
    @Bean
    fun commandLineRunner(): CommandLineRunner {
        return CommandLineRunner {
            logger.info("Running Batch Module")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<SpringKotlinMultiModule2Application>(*args)
}
