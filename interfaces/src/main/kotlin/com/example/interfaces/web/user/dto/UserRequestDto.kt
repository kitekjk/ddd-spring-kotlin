package com.example.interfaces.web.user.dto

import com.example.application.user.dto.CreateUserCommand
import com.example.application.user.dto.DeleteUserCommand
import com.example.application.user.dto.UpdatePasswordCommand
import com.example.application.user.dto.UpdateUserCommand

/**
 * Request DTO for creating a new user.
 */
data class CreateUserRequest(
    val name: String,
    val email: String,
    val loginId: String,
    val password: String
) {
    /**
     * Converts this request to a command.
     */
    fun toCommand(createdBy: String): CreateUserCommand {
        return CreateUserCommand(
            name = name,
            email = email,
            loginId = loginId,
            password = password,
            createdBy = createdBy
        )
    }
}

/**
 * Request DTO for updating a user.
 */
data class UpdateUserRequest(
    val name: String,
    val email: String
) {
    /**
     * Converts this request to a command.
     */
    fun toCommand(id: Long, updatedBy: String): UpdateUserCommand {
        return UpdateUserCommand(
            id = id,
            name = name,
            email = email,
            updatedBy = updatedBy
        )
    }
}

/**
 * Request DTO for updating a user's password.
 */
data class UpdatePasswordRequest(
    val newPassword: String
) {
    /**
     * Converts this request to a command.
     */
    fun toCommand(id: Long, updatedBy: String): UpdatePasswordCommand {
        return UpdatePasswordCommand(
            id = id,
            newPassword = newPassword,
            updatedBy = updatedBy
        )
    }
}

/**
 * Converts a user ID to a delete command.
 */
fun Long.toDeleteUserCommand(): DeleteUserCommand {
    return DeleteUserCommand(id = this)
}