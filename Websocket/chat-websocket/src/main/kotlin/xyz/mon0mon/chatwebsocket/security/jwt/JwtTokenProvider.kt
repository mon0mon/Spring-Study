package xyz.mon0mon.chatwebsocket.security.jwt

import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import xyz.mon0mon.chatwebsocket.domain.support.extension.findByIdOrThrow
import xyz.mon0mon.chatwebsocket.repository.user.UserRepository
import xyz.mon0mon.chatwebsocket.security.AccessTokenService
import xyz.mon0mon.chatwebsocket.security.DefaultUserDetails
import java.util.*

@Component
class JwtTokenProvider(
    private val accessTokenService: AccessTokenService,
    private val userRepository: UserRepository,
    @Value("\${security.jwt.token.secret-key:secret-key}")
    private var secretKey: String
) {
    fun getAuthentication(token: String): Authentication {
        accessTokenService.validate(token)

        val userId = accessTokenService.getUserId(token)

        val user = userRepository.findByIdOrThrow(userId)
        val principal = DefaultUserDetails(userId, user.role)

        return UsernamePasswordAuthenticationToken(principal, "", principal.authorities)
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims =
                Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .body

            val expirationDate = claims.expiration
            val currentDate = Date()

            if (expirationDate.before(currentDate)) {
                return false
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }
}
