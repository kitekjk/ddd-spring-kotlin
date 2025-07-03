package com.example.application.user

import com.example.domain.model.user.User
import com.example.domain.model.user.UserId

interface GetUserUseCase {
    fun getById(id: UserId): User?
    fun getByLoginId(loginId: String): User?
    fun getAll(): List<User>
}