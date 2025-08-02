package xyz.mon0mon.webauthn.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DashboardController {

    @GetMapping
    fun index(): String = "Hello World!"
}
