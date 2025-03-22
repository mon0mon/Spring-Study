package xyz.mon0mon.chatwebsocket.security.jwt

import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class JwtTokenFilterConfigurer(
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtTokenService: JwtTokenService
) : SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
    override fun configure(builder: HttpSecurity) {
        val customFilter = JwtTokenFilter(jwtTokenProvider, jwtTokenService)

        builder.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter::class.java)
    }
}
