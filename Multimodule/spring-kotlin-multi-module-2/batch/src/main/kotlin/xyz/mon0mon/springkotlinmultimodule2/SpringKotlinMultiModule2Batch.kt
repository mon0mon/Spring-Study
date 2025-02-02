package xyz.mon0mon.springkotlinmultimodule2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringKotlinMultiModule2Application

fun main(args: Array<String>) {
    runApplication<SpringKotlinMultiModule2Application>(*args)
}
