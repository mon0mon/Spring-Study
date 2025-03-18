package xyz.mon0mon.chatsample.security.jwt

import org.springframework.http.HttpStatus

class JwtSecurityException(override val message: String, val httpStatus: HttpStatus) : RuntimeException()
