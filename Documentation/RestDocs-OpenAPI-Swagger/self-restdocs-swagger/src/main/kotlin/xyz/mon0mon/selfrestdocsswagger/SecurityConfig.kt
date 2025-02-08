package xyz.mon0mon.selfrestdocsswagger

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    /**
     * 1) Swagger용 Security Filter Chain
     *    - Swagger UI, openapi.json 등 문서 페이지 접속 시 Form Login으로 인증
     */
    @Bean
    @Order(1)
    fun swaggerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            // Swagger 페이지에만 적용할 경로 설정
            .securityMatcher("/swagger/**", "/docs/**", "/login", "/error", "/logout", "/default-ui.css")
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/swagger/**", "/docs/**", "/logout").authenticated()
                auth.anyRequest().permitAll()
            }
            //  폼 로그인 사용
            .formLogin {  }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .csrf { it.disable() }
            .build()
    }

    /**
     * 2) 나머지 API용 Security Filter Chain
     *    - JWT 토큰 인증 적용
     */
    @Bean
    @Order(2)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .build()

        // JWT 인증 필터 로직(커스텀)을 추가하거나,
        // Spring Security의 JwtConfigurer / OAuth2 Resource Server 등을 사용할 수도 있음
        // 예) http.addFilterBefore(JwtAuthFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }

    /**
     * (테스트 용) Swagger Form Login 전용 In-Memory 계정
     * JWT 인증과는 별개로, Swagger 페이지 접속 시 사용할 수 있도록 설정
     */
    @Bean
    fun swaggerUserDetailsService(): UserDetailsService {
        val user = User.builder()
            .username("user")
            .password("{noop}1234")
            .roles("SWAGGER")
            .build()
        return InMemoryUserDetailsManager(user)
    }
}
