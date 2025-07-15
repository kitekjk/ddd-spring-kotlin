package com.example.infrastructure.persistence.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for UserEntity.
 */
@Repository
interface JpaUserRepository : JpaRepository<UserEntity, Long> {
    /**
     * Finds a user by login ID.
     */
    fun findByLoginId(loginId: String): UserEntity?
    
    /**
     * Finds a user by email.
     */
    fun findByEmail(email: String): UserEntity?
}