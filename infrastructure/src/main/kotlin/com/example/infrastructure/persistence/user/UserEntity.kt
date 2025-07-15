package com.example.infrastructure.persistence.user

import com.example.domain.model.common.AuditInfo
import com.example.domain.model.user.User
import com.example.domain.model.user.UserId
import jakarta.persistence.*
import java.time.Instant

/**
 * JPA entity for User.
 */
@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false, unique = true)
    var loginId: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var createdAt: Instant,

    @Column(nullable = false)
    var createdBy: String,

    @Column(nullable = false)
    var updatedAt: Instant,

    @Column(nullable = false)
    var updatedBy: String
) {
    /**
     * Converts this JPA entity to a domain User.
     */
    fun toDomain(): User {
        val userId = id?.let { UserId.of(it) }
        val auditInfo = AuditInfo.of(
            createdAt = createdAt,
            createdBy = createdBy,
            updatedAt = updatedAt,
            updatedBy = updatedBy
        )

        return if (userId != null) {
            User.reconstitute(
                id = userId,
                name = name,
                email = email,
                loginId = loginId,
                password = password,
                auditInfo = auditInfo
            )
        } else {
            throw IllegalStateException("Cannot convert UserEntity to User: ID is null")
        }
    }

    companion object {
        /**
         * Creates a UserEntity from a domain User.
         */
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                id = user.id?.value,
                name = user.name,
                email = user.email,
                loginId = user.loginId,
                password = user.getPassword(),
                createdAt = user.auditInfo.createdAt,
                createdBy = user.auditInfo.createdBy,
                updatedAt = user.auditInfo.updatedAt,
                updatedBy = user.auditInfo.updatedBy
            )
        }
    }
}
