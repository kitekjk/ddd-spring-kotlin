package com.example.domain.model.user

@JvmInline
value class UserId(val value: Long) {
    companion object {
        fun create(): UserId = UserId(0) // Initial value, will be replaced by DB
    }
}