package com.example.interfaces.web.user

import com.example.application.user.CreateUserCommand
import com.example.application.user.UpdateUserCommand
import com.example.domain.model.user.UserId
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateUserRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,
    
    @field:NotBlank(message = "Login ID is required")
    @field:Size(min = 4, max = 50, message = "Login ID must be between 4 and 50 characters")
    val loginId: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    val password: String
) {
    fun toCommand(): CreateUserCommand {
        return CreateUserCommand(
            name = name,
            email = email,
            loginId = loginId,
            password = password
        )
    }
}

data class UpdateUserRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null,
    
    @field:Email(message = "Email must be valid")
    val email: String? = null,
    
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    val password: String? = null
) {
    fun toCommand(id: UserId): UpdateUserCommand {
        return UpdateUserCommand(
            id = id,
            name = name,
            email = email,
            password = password
        )
    }
}