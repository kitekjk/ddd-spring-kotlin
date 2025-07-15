package com.example.interfaces.web.user.dto

import com.example.application.user.dto.UserDto
import java.time.Instant

/**
 * Response DTO for a user.
 */
data class UserResponse(
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
         * Creates a UserResponse from a UserDto.
         */
        fun from(userDto: UserDto): UserResponse {
            return UserResponse(
                id = userDto.id,
                name = userDto.name,
                email = userDto.email,
                loginId = userDto.loginId,
                createdAt = userDto.createdAt,
                createdBy = userDto.createdBy,
                updatedAt = userDto.updatedAt,
                updatedBy = userDto.updatedBy
            )
        }
    }
}

/**
 * Response DTO for a list of users.
 */
data class UserListResponse(
    val users: List<UserResponse>
) {
    companion object {
        /**
         * Creates a UserListResponse from a list of UserDtos.
         */
        fun from(userDtos: List<UserDto>): UserListResponse {
            return UserListResponse(
                users = userDtos.map { UserResponse.from(it) }
            )
        }
    }
}
