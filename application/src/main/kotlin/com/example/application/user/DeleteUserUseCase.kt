package com.example.application.user

import com.example.domain.model.user.UserId

interface DeleteUserUseCase {
    fun execute(id: UserId): Boolean
}