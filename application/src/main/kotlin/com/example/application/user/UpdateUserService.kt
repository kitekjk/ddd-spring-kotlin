package com.example.application.user

import com.example.domain.model.user.User
import com.example.domain.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateUserService(
    private val userRepository: UserRepository
) : UpdateUserUseCase {

    @Transactional
    override fun execute(command: UpdateUserCommand): User? {
        val user = userRepository.findById(command.id) ?: return null
        
        var updated = false
        
        command.name?.let {
            user.updateName(it)
            updated = true
        }
        
        command.email?.let {
            // Check if email is already used by another user
            userRepository.findByEmail(it)?.let { existingUser ->
                if (existingUser.id != user.id) {
                    throw IllegalArgumentException("Email ${it} is already in use")
                }
            }
            user.updateEmail(it)
            updated = true
        }
        
        command.password?.let {
            user.updatePassword(it)
            updated = true
        }
        
        return if (updated) userRepository.save(user) else user
    }
}