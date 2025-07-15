package com.example.application.user

import com.example.application.user.dto.UserDto
import com.example.domain.model.user.UserId
import com.example.domain.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Application service for retrieving users.
 */
@Service
@Transactional(readOnly = true)
class GetUserAppService(
    private val userRepository: UserRepository
) {
    /**
     * Gets a user by ID.
     * 
     * @param id The ID of the user to retrieve
     * @return The user as a DTO, or null if not found
     */
    fun getById(id: Long): UserDto? {
        val userId = UserId.of(id)
        return userRepository.findById(userId)?.let { UserDto.from(it) }
    }
    
    /**
     * Gets a user by login ID.
     * 
     * @param loginId The login ID of the user to retrieve
     * @return The user as a DTO, or null if not found
     */
    fun getByLoginId(loginId: String): UserDto? {
        return userRepository.findByLoginId(loginId)?.let { UserDto.from(it) }
    }
    
    /**
     * Gets a user by email.
     * 
     * @param email The email of the user to retrieve
     * @return The user as a DTO, or null if not found
     */
    fun getByEmail(email: String): UserDto? {
        return userRepository.findByEmail(email)?.let { UserDto.from(it) }
    }
    
    /**
     * Gets all users.
     * 
     * @return A list of all users as DTOs
     */
    fun getAll(): List<UserDto> {
        return userRepository.findAll().map { UserDto.from(it) }
    }
}