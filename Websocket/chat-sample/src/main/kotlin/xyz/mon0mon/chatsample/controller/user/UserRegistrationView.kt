package xyz.mon0mon.chatsample.controller.user

data class UserRegistrationViewReq(
    val email: String,
    val password: String,
    val name: String
)
