package com.example.infrastructure.persistence.user

import com.example.domain.model.user.User
import com.example.domain.model.user.UserId
import com.example.domain.model.user.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository
) : UserRepository {

    override fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        val savedEntity = jpaUserRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: UserId): User? {
        return jpaUserRepository.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findAll(): List<User> {
        return jpaUserRepository.findAll().map { it.toDomain() }
    }

    override fun findByLoginId(loginId: String): User? {
        return jpaUserRepository.findByLoginId(loginId)?.toDomain()
    }

    override fun findByEmail(email: String): User? {
        return jpaUserRepository.findByEmail(email)?.toDomain()
    }

    override fun delete(user: User) {
        jpaUserRepository.deleteById(user.id.value)
    }

    override fun deleteById(id: UserId) {
        jpaUserRepository.deleteById(id.value)
    }
}