package xyz.mon0mon.springfeatureflags

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [ConfigProperties::class])
class SpringFeatureFlagsApplication

fun main(args: Array<String>) {
    runApplication<SpringFeatureFlagsApplication>(*args)
}
