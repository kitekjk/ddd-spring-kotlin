package com.example.domain.model.user

import com.example.domain.model.common.AuditInfo

/**
 * User aggregate root representing a user in the system.
 * Contains user identity and profile information.
 */
class User private constructor(
    val id: UserId?,
    val name: String,
    val email: String,
    val loginId: String,
    private var password: String,
    val auditInfo: AuditInfo
) {
    companion object {
        /**
         * Creates a new User instance.
         */
        fun create(
            name: String,
            email: String,
            loginId: String,
            password: String,
            createdBy: String
        ): User {
            require(name.isNotBlank()) { "Name cannot be blank" }
            require(email.isNotBlank()) { "Email cannot be blank" }
            require(loginId.isNotBlank()) { "Login ID cannot be blank" }
            require(password.isNotBlank()) { "Password cannot be blank" }

            return User(
                id = null,
                name = name,
                email = email,
                loginId = loginId,
                password = password,
                auditInfo = AuditInfo.newInstance(createdBy)
            )
        }

        /**
         * Reconstitutes a User from persistence.
         */
        fun reconstitute(
            id: UserId,
            name: String,
            email: String,
            loginId: String,
            password: String,
            auditInfo: AuditInfo
        ): User {
            return User(
                id = id,
                name = name,
                email = email,
                loginId = loginId,
                password = password,
                auditInfo = auditInfo
            )
        }
    }

    /**
     * Updates the user's information.
     */
    fun update(
        name: String,
        email: String,
        updatedBy: String
    ): User {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }

        return User(
            id = this.id,
            name = name,
            email = email,
            loginId = this.loginId,
            password = this.password,
            auditInfo = this.auditInfo.update(updatedBy)
        )
    }

    /**
     * Updates the user's password.
     */
    fun updatePassword(
        newPassword: String,
        updatedBy: String
    ): User {
        require(newPassword.isNotBlank()) { "Password cannot be blank" }

        return User(
            id = this.id,
            name = this.name,
            email = this.email,
            loginId = this.loginId,
            password = newPassword,
            auditInfo = this.auditInfo.update(updatedBy)
        )
    }

    /**
     * Verifies if the provided password matches the user's password.
     */
    fun verifyPassword(password: String): Boolean {
        return this.password == password
    }

    /**
     * Gets the user's password. This method should be used carefully and only when necessary.
     */
    fun getPassword(): String = password
}
