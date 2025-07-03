package com.example.domain.model.user

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UserId): User?
    fun findAll(): List<User>
    fun findByLoginId(loginId: String): User?
    fun findByEmail(email: String): User?
    fun delete(user: User)
    fun deleteById(id: UserId)
}