package xyz.mon0mon.chatwebsocket.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import xyz.mon0mon.chatwebsocket.domain.user.Role

interface CustomUserDetails {
    val userId: Long
    val role: Role
}

class DefaultUserDetails(
    override val userId: Long,
    override val role: Role
) : CustomUserDetails, UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        val authorities: MutableList<GrantedAuthority> = mutableListOf()

        authorities.add(SimpleGrantedAuthority(role.value))

        return authorities
    }

    override fun getPassword(): String = ""

    override fun getUsername(): String = userId.toString()

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
