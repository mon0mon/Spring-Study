package xyz.mon0mon.springkotlinmultimodule2

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger: Logger = LoggerFactory.getLogger(BatchSample::class.java)

@Component
class BatchSample {
    @Scheduled(fixedRate = 2000)
    fun hello() {
        logger.info("running job every 2 sec")
    }
}
