package xyz.mon0mon.chatsample.controller.user

import xyz.mon0mon.chatsample.domain.user.User

data class UserLoginViewReq(
    val email: String,
    val password: String
)

data class UserLoginViewRes(
    val accessToken: String,
    val user: User
)
