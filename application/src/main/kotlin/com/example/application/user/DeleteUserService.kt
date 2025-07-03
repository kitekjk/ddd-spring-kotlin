package com.example.application.user

import com.example.domain.model.user.UserId
import com.example.domain.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteUserService(
    private val userRepository: UserRepository
) : DeleteUserUseCase {

    @Transactional
    override fun execute(id: UserId): Boolean {
        val user = userRepository.findById(id) ?: return false
        userRepository.delete(user)
        return true
    }
}