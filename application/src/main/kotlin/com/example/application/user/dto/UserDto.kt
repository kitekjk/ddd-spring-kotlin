package com.example.application.user.dto

import com.example.domain.model.user.User
import java.time.Instant

/**
 * DTO for User data.
 */
data class UserDto(
    val id: Long?,
    val name: String,
    val email: String,
    val loginId: String,
    val createdAt: Instant,
    val createdBy: String,
    val updatedAt: Instant,
    val updatedBy: String
) {
    companion object {
        /**
         * Creates a UserDto from a User domain object.
         */
        fun from(user: User): UserDto {
            return UserDto(
                id = user.id?.value,
                name = user.name,
                email = user.email,
                loginId = user.loginId,
                createdAt = user.auditInfo.createdAt,
                createdBy = user.auditInfo.createdBy,
                updatedAt = user.auditInfo.updatedAt,
                updatedBy = user.auditInfo.updatedBy
            )
        }
    }
}

/**
 * Command for creating a new user.
 */
data class CreateUserCommand(
    val name: String,
    val email: String,
    val loginId: String,
    val password: String,
    val createdBy: String
)

/**
 * Command for updating a user.
 */
data class UpdateUserCommand(
    val id: Long,
    val name: String,
    val email: String,
    val updatedBy: String
)

/**
 * Command for updating a user's password.
 */
data class UpdatePasswordCommand(
    val id: Long,
    val newPassword: String,
    val updatedBy: String
)

/**
 * Command for deleting a user.
 */
data class DeleteUserCommand(
    val id: Long
)
