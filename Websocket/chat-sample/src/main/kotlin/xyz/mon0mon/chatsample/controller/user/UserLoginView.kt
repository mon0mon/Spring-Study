package xyz.mon0mon.chatsample.controller.user

data class UserLoginViewReq(
    val email: String,
    val password: String
)

data class UserLoginViewRes(
    val accessToken: String
)
