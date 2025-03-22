package xyz.mon0mon.chatwebsocket.controller.user

import xyz.mon0mon.chatwebsocket.domain.user.User

data class UserLoginViewReq(
    val email: String,
    val password: String
)

data class UserLoginViewRes(
    val accessToken: String,
    val user: User
)
