package xyz.mon0mon.chatwebsocket.controller.user

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.mon0mon.chatwebsocket.service.user.UserService

@RestController
class UserController(
    private val userService: UserService
) {

    @PostMapping("/users/login")
    fun login(@RequestBody req: UserLoginViewReq): UserLoginViewRes {
        val (accessToken, user) = userService.login(req.email, req.password)

        return UserLoginViewRes(accessToken = accessToken, user = user)
    }

    @PostMapping("/users")
    fun register(@RequestBody req: UserRegistrationViewReq) {
        userService.register(email = req.email, password = req.password, name = req.name)
    }
}
