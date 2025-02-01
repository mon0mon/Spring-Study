package xyz.mon0mon.springfeatureflags

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FeaturesConfigController(
    private val properties: ConfigProperties
) {
    @GetMapping("/feature-flags")
    fun getProperties(): ConfigProperties = properties
}
