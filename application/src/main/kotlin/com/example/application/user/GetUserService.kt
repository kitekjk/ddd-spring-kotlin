package com.example.application.user

import com.example.domain.model.user.User
import com.example.domain.model.user.UserId
import com.example.domain.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetUserService(
    private val userRepository: UserRepository
) : GetUserUseCase {

    @Transactional(readOnly = true)
    override fun getById(id: UserId): User? {
        return userRepository.findById(id)
    }

    @Transactional(readOnly = true)
    override fun getByLoginId(loginId: String): User? {
        return userRepository.findByLoginId(loginId)
    }

    @Transactional(readOnly = true)
    override fun getAll(): List<User> {
        return userRepository.findAll()
    }
}