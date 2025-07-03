package com.example.domain.model.user

import java.time.LocalDateTime

class User private constructor(
    val id: UserId,
    private var name: String,
    private var email: String,
    private var loginId: String,
    private var password: String,
    val createdAt: LocalDateTime,
    private var updatedAt: LocalDateTime
) {
    fun getName(): String = name
    fun getEmail(): String = email
    fun getLoginId(): String = loginId
    fun getPassword(): String = password
    fun getUpdatedAt(): LocalDateTime = updatedAt

    fun updateName(name: String): User {
        this.name = name
        this.updatedAt = LocalDateTime.now()
        return this
    }

    fun updateEmail(email: String): User {
        this.email = email
        this.updatedAt = LocalDateTime.now()
        return this
    }

    fun updatePassword(password: String): User {
        this.password = password
        this.updatedAt = LocalDateTime.now()
        return this
    }

    companion object {
        fun create(
            name: String,
            email: String,
            loginId: String,
            password: String
        ): User {
            val now = LocalDateTime.now()
            return User(
                id = UserId.create(),
                name = name,
                email = email,
                loginId = loginId,
                password = password,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}