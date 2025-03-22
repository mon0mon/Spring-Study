package xyz.mon0mon.chatwebsocket.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

/**
 * 로그인 컨텍스트
 */
class DefaultSecurityContext {
    companion object {
        fun user(): CustomUserDetails? {
            val authentication = SecurityContextHolder.getContext().authentication ?: return null

            return if (isUser(authentication)) {
                authentication.principal as CustomUserDetails
            } else {
                null
            }
        }

        fun userId(): Long? {
            return user()?.userId
        }

        fun getContext(): SecurityContext? {
            return SecurityContextHolder.getContext()
        }

        private fun isUser(authentication: Authentication): Boolean {
            return authentication.principal !is String
        }
    }
}
