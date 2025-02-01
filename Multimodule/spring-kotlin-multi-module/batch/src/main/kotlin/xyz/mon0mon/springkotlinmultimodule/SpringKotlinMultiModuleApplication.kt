package xyz.mon0mon.springkotlinmultimodule

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringKotlinMultiModuleApplication

fun main(args: Array<String>) {
    runApplication<SpringKotlinMultiModuleApplication>(*args)
}
