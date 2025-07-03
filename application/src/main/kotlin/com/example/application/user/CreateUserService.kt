package com.example.application.user

import com.example.domain.model.user.User
import com.example.domain.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateUserService(
    private val userRepository: UserRepository
) : CreateUserUseCase {

    @Transactional
    override fun execute(command: CreateUserCommand): User {
        // Check if user with same loginId or email already exists
        userRepository.findByLoginId(command.loginId)?.let {
            throw IllegalArgumentException("User with login ID ${command.loginId} already exists")
        }
        
        userRepository.findByEmail(command.email)?.let {
            throw IllegalArgumentException("User with email ${command.email} already exists")
        }
        
        // Create and save new user
        val user = User.create(
            name = command.name,
            email = command.email,
            loginId = command.loginId,
            password = command.password
        )
        
        return userRepository.save(user)
    }
}