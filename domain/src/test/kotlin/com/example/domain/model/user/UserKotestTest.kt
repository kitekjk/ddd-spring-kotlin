package com.example.domain.model.user

import com.example.domain.model.common.AuditInfo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import java.time.Instant

class UserKotestTest : StringSpec({
    // Test constants
    val validName = "Test User"
    val validEmail = "test@example.com"
    val validLoginId = "testuser"
    val validPassword = "password123"
    val validCreator = "system"
    
    "should create user with valid inputs" {
        // When
        val user = User.create(
            name = validName,
            email = validEmail,
            loginId = validLoginId,
            password = validPassword,
            createdBy = validCreator
        )
        
        // Then
        user.id shouldBe null
        user.name shouldBe validName
        user.email shouldBe validEmail
        user.loginId shouldBe validLoginId
        user.verifyPassword(validPassword) shouldBe true
        user.auditInfo.createdBy shouldBe validCreator
        user.auditInfo.updatedBy shouldBe validCreator
    }
    
    "should throw exception when creating user with blank name" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            User.create(
                name = "",
                email = validEmail,
                loginId = validLoginId,
                password = validPassword,
                createdBy = validCreator
            )
        }
    }
    
    "should throw exception when creating user with blank email" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            User.create(
                name = validName,
                email = "",
                loginId = validLoginId,
                password = validPassword,
                createdBy = validCreator
            )
        }
    }
    
    "should throw exception when creating user with blank loginId" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            User.create(
                name = validName,
                email = validEmail,
                loginId = "",
                password = validPassword,
                createdBy = validCreator
            )
        }
    }
    
    "should throw exception when creating user with blank password" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            User.create(
                name = validName,
                email = validEmail,
                loginId = validLoginId,
                password = "",
                createdBy = validCreator
            )
        }
    }
    
    "should reconstitute user from persistence" {
        // Given
        val userId = UserId.of(1L)
        val now = Instant.now()
        val auditInfo = AuditInfo.of(now, validCreator, now, validCreator)
        
        // When
        val user = User.reconstitute(
            id = userId,
            name = validName,
            email = validEmail,
            loginId = validLoginId,
            password = validPassword,
            auditInfo = auditInfo
        )
        
        // Then
        user.id shouldBe userId
        user.name shouldBe validName
        user.email shouldBe validEmail
        user.loginId shouldBe validLoginId
        user.verifyPassword(validPassword) shouldBe true
        user.auditInfo shouldBe auditInfo
    }
    
    "should update user information" {
        // Given
        val user = User.create(
            name = validName,
            email = validEmail,
            loginId = validLoginId,
            password = validPassword,
            createdBy = validCreator
        )
        val newName = "Updated Name"
        val newEmail = "updated@example.com"
        val updater = "admin"
        
        // When
        val updatedUser = user.update(
            name = newName,
            email = newEmail,
            updatedBy = updater
        )
        
        // Then
        updatedUser.name shouldBe newName
        updatedUser.email shouldBe newEmail
        updatedUser.loginId shouldBe validLoginId
        updatedUser.verifyPassword(validPassword) shouldBe true
        updatedUser.auditInfo.createdBy shouldBe validCreator
        updatedUser.auditInfo.updatedBy shouldBe updater
    }
    
    "should throw exception when updating user with blank name" {
        // Given
        val user = User.create(
            name = validName,
            email = validEmail,
            loginId = validLoginId,
            password = validPassword,
            createdBy = validCreator
        )
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            user.update(
                name = "",
                email = validEmail,
                updatedBy = "admin"
            )
        }
    }
    
    "should throw exception when updating user with blank email" {
        // Given
        val user = User.create(
            name = validName,
            email = validEmail,
            loginId = validLoginId,
            password = validPassword,
            createdBy = validCreator
        )
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            user.update(
                name = validName,
                email = "",
                updatedBy = "admin"
            )
        }
    }
    
    "should update user password" {
        // Given
        val user = User.create(
            name = validName,
            email = validEmail,
            loginId = validLoginId,
            password = validPassword,
            createdBy = validCreator
        )
        val newPassword = "newPassword456"
        val updater = "admin"
        
        // When
        val updatedUser = user.updatePassword(
            newPassword = newPassword,
            updatedBy = updater
        )
        
        // Then
        updatedUser.verifyPassword(validPassword) shouldBe false
        updatedUser.verifyPassword(newPassword) shouldBe true
        updatedUser.auditInfo.updatedBy shouldBe updater
    }
    
    "should throw exception when updating password with blank value" {
        // Given
        val user = User.create(
            name = validName,
            email = validEmail,
            loginId = validLoginId,
            password = validPassword,
            createdBy = validCreator
        )
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            user.updatePassword(
                newPassword = "",
                updatedBy = "admin"
            )
        }
    }
    
    "should verify correct password" {
        // Given
        val user = User.create(
            name = validName,
            email = validEmail,
            loginId = validLoginId,
            password = validPassword,
            createdBy = validCreator
        )
        
        // When & Then
        user.verifyPassword(validPassword) shouldBe true
    }
    
    "should not verify incorrect password" {
        // Given
        val user = User.create(
            name = validName,
            email = validEmail,
            loginId = validLoginId,
            password = validPassword,
            createdBy = validCreator
        )
        
        // When & Then
        user.verifyPassword("wrongPassword") shouldBe false
    }
    
    "should get password" {
        // Given
        val user = User.create(
            name = validName,
            email = validEmail,
            loginId = validLoginId,
            password = validPassword,
            createdBy = validCreator
        )
        
        // When
        val password = user.getPassword()
        
        // Then
        password shouldBe validPassword
    }
})