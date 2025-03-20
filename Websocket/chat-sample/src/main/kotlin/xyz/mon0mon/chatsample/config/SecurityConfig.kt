package xyz.mon0mon.chatsample.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.CorsUtils
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import xyz.mon0mon.chatsample.security.jwt.JwtTokenFilter

@Configuration
class SecurityConfig(
    private val jwtTokenFilter: JwtTokenFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.cors {}
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests { authorize ->
                authorize.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                authorize.requestMatchers("/ws/**").permitAll()
                authorize.requestMatchers("/index.html", "/js/**", "/css/**", "/img/**").permitAll()
                authorize.requestMatchers(
                    HttpMethod.POST,
                    "/users", "/users/login"
                ).permitAll()
                authorize.anyRequest().authenticated()
            }
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter::class.java).build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = mutableListOf("*")
            allowCredentials = true
            allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH")
            allowedHeaders =
                listOf(
                    "Authorization", "Cache-Control", "Content-Type", "lang", "X-Forwarded-For", "Api-Key",
                    "x-auth-token"
                )
            maxAge = 3600
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
