package com.example.infrastructure.persistence.user

import com.example.domain.model.user.User
import com.example.domain.model.user.UserId
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false, unique = true)
    var loginId: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime,

    @Column(nullable = false)
    var updatedAt: LocalDateTime
) {
    fun toDomain(): User {
        return User.create(
            name = name,
            email = email,
            loginId = loginId,
            password = password
        ).let {
            // Use reflection to set the id, createdAt and updatedAt fields
            val userClass = User::class.java

            val idField = userClass.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(it, UserId(id))

            val createdAtField = userClass.getDeclaredField("createdAt")
            createdAtField.isAccessible = true
            createdAtField.set(it, createdAt)

            val updatedAtField = userClass.getDeclaredField("updatedAt")
            updatedAtField.isAccessible = true
            updatedAtField.set(it, updatedAt)

            it
        }
    }

    companion object {
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                id = user.id.value,
                name = user.getName(),
                email = user.getEmail(),
                loginId = user.getLoginId(),
                password = user.getPassword(),
                createdAt = user.createdAt,
                updatedAt = user.getUpdatedAt()
            )
        }
    }
}
