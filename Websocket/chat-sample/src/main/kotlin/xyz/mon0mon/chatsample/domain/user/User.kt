package xyz.mon0mon.chatsample.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import xyz.mon0mon.chatsample.domain.support.jpa.BaseAggregateRoot

@Entity
class User (
    @Column(length = 20, unique = true, nullable = false)
    var name: String,

    @Column(length = 100, unique = true, nullable = false)
    val email: String,

    @Column(length = 100, nullable = false)
    var password: String,

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    var role: Role = Role.USER
): BaseAggregateRoot<User>() {
    fun update(name: String, password: String) {
        this.name = name
        this.password = password
    }
}

enum class Role(
    val value: String
) {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");
}
