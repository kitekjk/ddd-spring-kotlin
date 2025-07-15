package com.example.domain.model.user

/**
 * Value object representing a unique identifier for a User.
 */
@JvmInline
value class UserId private constructor(val value: Long) {
    companion object {
        /**
         * Creates a UserId with the given value.
         */
        fun of(value: Long): UserId {
            require(value > 0) { "User ID must be positive" }
            return UserId(value)
        }
    }

    override fun toString(): String = value.toString()
}