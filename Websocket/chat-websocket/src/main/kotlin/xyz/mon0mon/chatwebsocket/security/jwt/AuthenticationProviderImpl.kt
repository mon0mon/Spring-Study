package xyz.mon0mon.chatwebsocket.security.jwt

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderNotFoundException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import xyz.mon0mon.chatwebsocket.security.CustomUserDetails

@Component
class AuthenticationProviderImpl : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        if (authentication.principal !is CustomUserDetails) throw ProviderNotFoundException("No JWT Token")

        val customUserDetails = authentication.principal as CustomUserDetails

        return UsernamePasswordAuthenticationToken(customUserDetails, null, mutableListOf())
    }

    override fun supports(authentication: Class<*>?) = authentication == UsernamePasswordAuthenticationToken::class.java
}
