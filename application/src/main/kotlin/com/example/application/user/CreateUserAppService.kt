package com.example.application.user

import com.example.application.user.dto.CreateUserCommand
import com.example.application.user.dto.UserDto
import com.example.domain.model.user.User
import com.example.domain.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Application service for creating a new user.
 */
@Service
@Transactional
class CreateUserAppService(
    private val userRepository: UserRepository
) {
    /**
     * Creates a new user with the given information.
     * 
     * @param command The command containing the user information
     * @return The created user as a DTO
     * @throws IllegalArgumentException if a user with the same login ID or email already exists
     */
    fun execute(command: CreateUserCommand): UserDto {
        // Check if a user with the same login ID already exists
        userRepository.findByLoginId(command.loginId)?.let {
            throw IllegalArgumentException("User with login ID ${command.loginId} already exists")
        }
        
        // Check if a user with the same email already exists
        userRepository.findByEmail(command.email)?.let {
            throw IllegalArgumentException("User with email ${command.email} already exists")
        }
        
        // Create a new user
        val user = User.create(
            name = command.name,
            email = command.email,
            loginId = command.loginId,
            password = command.password,
            createdBy = command.createdBy
        )
        
        // Save the user
        val savedUser = userRepository.save(user)
        
        // Return the user as a DTO
        return UserDto.from(savedUser)
    }
}