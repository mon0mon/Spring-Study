package xyz.mon0mon.springfeatureflags

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringFeatureFlagsApplication

fun main(args: Array<String>) {
    runApplication<SpringFeatureFlagsApplication>(*args)
}
