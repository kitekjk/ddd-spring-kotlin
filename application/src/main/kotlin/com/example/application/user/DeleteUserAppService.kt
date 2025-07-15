package com.example.application.user

import com.example.application.user.dto.DeleteUserCommand
import com.example.domain.model.user.UserId
import com.example.domain.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Application service for deleting users.
 */
@Service
@Transactional
class DeleteUserAppService(
    private val userRepository: UserRepository
) {
    /**
     * Deletes a user by ID.
     * 
     * @param command The command containing the ID of the user to delete
     * @return true if the user was deleted, false if the user was not found
     */
    fun execute(command: DeleteUserCommand): Boolean {
        val userId = UserId.of(command.id)
        return userRepository.deleteById(userId)
    }
}