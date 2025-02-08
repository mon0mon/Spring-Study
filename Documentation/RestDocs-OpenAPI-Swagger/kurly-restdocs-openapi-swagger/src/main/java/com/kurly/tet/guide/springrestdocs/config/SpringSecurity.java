package com.kurly.tet.guide.springrestdocs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringSecurity {

    /**
     * 1) Swagger용 Security Filter Chain
     * - Swagger UI, openapi.json 등 문서 페이지 접속 시 Basic Auth 진행
     */
    @Bean
    @Order(1)
    SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Swagger 페이지에만 적용할 경로 설정
                .securityMatcher(
                        "/swagger-ui/**", "/v3/api-docs/**", "/docs/**"
                )
                .authorizeHttpRequests(auth -> {
                    //  위 경로들은 인증 필요
                    auth.anyRequest().authenticated();
                })
                //  Basic Auth 사용
                .httpBasic(Customizer.withDefaults())
                //  JWT나 세션 관리는 여기서 사용하지 않음
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(it -> it.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * 2) 나머지 API용 Security Filter Chain
     * - JWT 토큰 인증 적용
     */
    @Bean
    @Order(2)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        // 나머지 경로들
//        http.authorizeHttpRequests( auth -> auth.anyRequest().authenticated())
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(it -> it.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // JWT 인증 필터 로직(커스텀)을 추가하거나,
        // Spring Security의 JwtConfigurer / OAuth2 Resource Server 등을 사용할 수도 있음
        // 예) http.addFilterBefore(JwtAuthFilter(), UsernamePasswordAuthenticationFilter::class.java)

        //  현재는 PermitAll()
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(it -> it.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * (테스트 용) Swagger Basic Auth 전용 In-Memory 계정
     * JWT 인증과는 별개로, Swagger 페이지 접속 시 사용할 수 있도록 설정
     */
    @Bean
    UserDetailsService swaggerUserDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password("{noop}1234")  // 실제 운영에서는 BCrypt 등으로 암호화
                .roles("SWAGGER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}
