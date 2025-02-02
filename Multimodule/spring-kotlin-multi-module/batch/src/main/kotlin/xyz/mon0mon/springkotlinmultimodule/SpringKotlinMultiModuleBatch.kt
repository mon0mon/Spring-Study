package xyz.mon0mon.springkotlinmultimodule

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger: Logger = LoggerFactory.getLogger(BatchSample::class.java)

@SpringBootApplication
@EnableScheduling
class SpringKotlinMultiModuleBatch

fun main(args: Array<String>) {
    runApplication<SpringKotlinMultiModuleBatch>(*args)
}

@Component
class BatchSample {
    @Scheduled(fixedRate = 2000)
    fun job() {
        logger.info("running job every 2 sec")
    }
}
