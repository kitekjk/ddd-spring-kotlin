package com.example.application.user

import com.example.domain.model.user.User

interface CreateUserUseCase {
    fun execute(command: CreateUserCommand): User
}

data class CreateUserCommand(
    val name: String,
    val email: String,
    val loginId: String,
    val password: String
)