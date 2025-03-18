package xyz.mon0mon.chatsample.security.jwt

import xyz.mon0mon.chatsample.security.CustomUserDetails
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderNotFoundException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class AuthenticationProviderImpl : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        if (authentication.principal !is CustomUserDetails) throw ProviderNotFoundException("No JWT Token")

        val customUserDetails = authentication.principal as CustomUserDetails

        return UsernamePasswordAuthenticationToken(customUserDetails, null, mutableListOf())
    }

    override fun supports(authentication: Class<*>?) =
        authentication == UsernamePasswordAuthenticationToken::class.java
}
