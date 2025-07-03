package com.example.interfaces.web.user

import com.example.domain.model.user.User
import java.time.LocalDateTime

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val loginId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id.value,
                name = user.getName(),
                email = user.getEmail(),
                loginId = user.getLoginId(),
                createdAt = user.createdAt,
                updatedAt = user.getUpdatedAt()
            )
        }
    }
}

data class UserListResponse(
    val users: List<UserResponse>
) {
    companion object {
        fun from(users: List<User>): UserListResponse {
            return UserListResponse(
                users = users.map { UserResponse.from(it) }
            )
        }
    }
}