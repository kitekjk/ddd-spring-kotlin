package com.example.application.user

import com.example.application.user.dto.UpdatePasswordCommand
import com.example.application.user.dto.UpdateUserCommand
import com.example.application.user.dto.UserDto
import com.example.domain.model.user.UserId
import com.example.domain.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Application service for updating users.
 */
@Service
@Transactional
class UpdateUserAppService(
    private val userRepository: UserRepository
) {
    /**
     * Updates a user's information.
     * 
     * @param command The command containing the updated user information
     * @return The updated user as a DTO
     * @throws IllegalArgumentException if the user does not exist or if a user with the same email already exists
     */
    fun execute(command: UpdateUserCommand): UserDto {
        // Get the user
        val userId = UserId.of(command.id)
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User with ID ${command.id} not found")
        
        // Check if a user with the same email already exists (and it's not the same user)
        userRepository.findByEmail(command.email)?.let {
            if (it.id != userId) {
                throw IllegalArgumentException("User with email ${command.email} already exists")
            }
        }
        
        // Update the user
        val updatedUser = user.update(
            name = command.name,
            email = command.email,
            updatedBy = command.updatedBy
        )
        
        // Save the user
        val savedUser = userRepository.save(updatedUser)
        
        // Return the user as a DTO
        return UserDto.from(savedUser)
    }
    
    /**
     * Updates a user's password.
     * 
     * @param command The command containing the new password
     * @return The updated user as a DTO
     * @throws IllegalArgumentException if the user does not exist
     */
    fun updatePassword(command: UpdatePasswordCommand): UserDto {
        // Get the user
        val userId = UserId.of(command.id)
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User with ID ${command.id} not found")
        
        // Update the password
        val updatedUser = user.updatePassword(
            newPassword = command.newPassword,
            updatedBy = command.updatedBy
        )
        
        // Save the user
        val savedUser = userRepository.save(updatedUser)
        
        // Return the user as a DTO
        return UserDto.from(savedUser)
    }
}