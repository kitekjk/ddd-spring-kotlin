package com.example.application.user

import com.example.domain.model.user.User
import com.example.domain.model.user.UserId

interface UpdateUserUseCase {
    fun execute(command: UpdateUserCommand): User?
}

data class UpdateUserCommand(
    val id: UserId,
    val name: String? = null,
    val email: String? = null,
    val password: String? = null
)