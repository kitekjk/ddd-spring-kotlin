package com.example.infrastructure.persistence.user

import com.example.domain.model.common.AuditInfo
import com.example.domain.model.user.User
import com.example.domain.model.user.UserId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import java.time.Instant
import java.time.temporal.ChronoUnit

class UserEntityKotestTest : StringSpec({
    "should convert from domain User with ID" {
        // Given
        val userId = UserId.of(123L)
        val name = "Test User"
        val email = "test@example.com"
        val loginId = "testuser"
        val password = "password123"
        val createdAt = Instant.now().minus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS)
        val createdBy = "creator"
        val updatedAt = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS)
        val updatedBy = "updater"
        val auditInfo = AuditInfo.of(createdAt, createdBy, updatedAt, updatedBy)

        val user = User.reconstitute(
            id = userId,
            name = name,
            email = email,
            loginId = loginId,
            password = password,
            auditInfo = auditInfo
        )

        // When
        val entity = UserEntity.fromDomain(user)

        // Then
        entity.id shouldBe userId.value
        entity.name shouldBe name
        entity.email shouldBe email
        entity.loginId shouldBe loginId
        entity.password shouldBe password
        entity.createdAt shouldBe createdAt
        entity.createdBy shouldBe createdBy
        entity.updatedAt shouldBe updatedAt
        entity.updatedBy shouldBe updatedBy
    }

    "should convert from domain User without ID" {
        // Given
        val name = "New User"
        val email = "new@example.com"
        val loginId = "newuser"
        val password = "newpassword123"
        val createdBy = "system"

        val user = User.create(
            name = name,
            email = email,
            loginId = loginId,
            password = password,
            createdBy = createdBy
        )

        // When
        val entity = UserEntity.fromDomain(user)

        // Then
        entity.id shouldBe null
        entity.name shouldBe name
        entity.email shouldBe email
        entity.loginId shouldBe loginId
        entity.password shouldBe password
        entity.createdBy shouldBe createdBy
        entity.updatedBy shouldBe createdBy
        // Timestamps are created dynamically, so we just verify they exist
        entity.createdAt shouldBe entity.updatedAt
    }

    "should convert to domain User when ID is present" {
        // Given
        val id = 123L
        val name = "Test User"
        val email = "test@example.com"
        val loginId = "testuser"
        val password = "password123"
        val createdAt = Instant.now().minus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS)
        val createdBy = "creator"
        val updatedAt = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS)
        val updatedBy = "updater"

        val entity = UserEntity(
            id = id,
            name = name,
            email = email,
            loginId = loginId,
            password = password,
            createdAt = createdAt,
            createdBy = createdBy,
            updatedAt = updatedAt,
            updatedBy = updatedBy
        )

        // When
        val user = entity.toDomain()

        // Then
        user.id?.value shouldBe id
        user.name shouldBe name
        user.email shouldBe email
        user.loginId shouldBe loginId
        user.getPassword() shouldBe password
        user.auditInfo.createdAt shouldBe createdAt
        user.auditInfo.createdBy shouldBe createdBy
        user.auditInfo.updatedAt shouldBe updatedAt
        user.auditInfo.updatedBy shouldBe updatedBy
    }

    "should throw exception when converting to domain User without ID" {
        // Given
        val entity = UserEntity(
            id = null,
            name = "Test User",
            email = "test@example.com",
            loginId = "testuser",
            password = "password123",
            createdAt = Instant.now(),
            createdBy = "creator",
            updatedAt = Instant.now(),
            updatedBy = "updater"
        )

        // When & Then
        shouldThrow<IllegalStateException> {
            entity.toDomain()
        }
    }
})
