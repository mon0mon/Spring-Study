package xyz.mon0mon.chatwebsocket.security.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import xyz.mon0mon.chatwebsocket.security.AccessTokenService

@Configuration
class JwtTokenFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val accessTokenService: AccessTokenService
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token: String? = resolveToken(request)

        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                accessTokenService.validate(token)

                val auth = jwtTokenProvider.getAuthentication(token)

                SecurityContextHolder.getContext().authentication = auth
            }
        } catch (ex: ExpiredJwtException) {
            // 🔥 Access Token이 만료되었을 때 401 반환
            SecurityContextHolder.clearContext()
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Token Expired")

            return
        } catch (ex: JwtException) {
            // 🔥 JWT가 잘못된 경우에도 401 반환
            SecurityContextHolder.clearContext()
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token")

            return
        } catch (ex: JwtSecurityException) {
            // 기존 예외 처리 (다른 보안 예외 발생 시)
            SecurityContextHolder.clearContext()
            response.sendError(ex.httpStatus.value(), ex.message)

            return
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(req: HttpServletRequest): String? {
        val bearerToken = req.getHeader("Authorization")

        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
