package xyz.mon0mon.chatwebsocket.security.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import xyz.mon0mon.chatwebsocket.domain.support.extension.findByIdOrThrow
import xyz.mon0mon.chatwebsocket.repository.user.UserRepository
import xyz.mon0mon.chatwebsocket.security.AccessTokenService
import java.util.Date

@Service
class JwtTokenService(
    @Value("\${security.jwt.token.secret-key:secret-key}")
    private var secretKey: String,
    @Value("\${security.jwt.token.expire-length:expire-length}")
    private val expireLength: Long,
    private val userRepository: UserRepository
) : AccessTokenService {
    override fun refresh(accessToken: String): String {
        val userId = getUserId(accessToken)

        return create(userId)
    }

    override fun create(userId: Long): String {
        val user = userRepository.findByIdOrThrow(userId)

        val now = Date()
        val validity = Date(now.time + expireLength)

        val keyBytes = Decoders.BASE64.decode(secretKey)
        val key = Keys.hmacShaKeyFor(keyBytes)

        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    override fun getUserId(accessToken: String): Long {
        return try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).body.subject.toLong()
        } catch (e: ExpiredJwtException) {
            e.claims!!.subject.toLong()
        }
    }

    override fun validate(accessToken: String) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken)
        } catch (e: IllegalArgumentException) {
            throw JwtSecurityException("invalid JWT token", HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (e: ExpiredJwtException) {
            throw JwtSecurityException("expired JWT token", HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (e: SignatureException) {
            throw JwtSecurityException("authentication fail", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
