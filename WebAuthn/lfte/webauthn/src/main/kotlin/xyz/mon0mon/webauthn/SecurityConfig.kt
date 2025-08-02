package xyz.mon0mon.webauthn

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val encoders: MutableMap<String, PasswordEncoder> = mutableMapOf()

        encoders["bcrypt"] = BCryptPasswordEncoder()
        encoders["noop"] = NoOpPasswordEncoder.getInstance()

        return DelegatingPasswordEncoder("noop", encoders)
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user = User.withUsername("user")
            .password("{noop}password")
            .roles("USER")
            .build()

        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it.anyRequest().authenticated()
            }
            .formLogin { }
            .logout { }
            .webAuthn { webAuthn ->
                webAuthn.rpName("Spring Security Relying Party")
                    .rpId("localhost")
                    .allowedOrigins("http://localhost:8080")
            }

        return http.build()
    }
}
