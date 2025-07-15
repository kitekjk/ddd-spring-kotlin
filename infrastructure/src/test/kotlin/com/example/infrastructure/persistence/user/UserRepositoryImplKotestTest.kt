package com.example.infrastructure.persistence.user

import com.example.domain.model.common.AuditInfo
import com.example.domain.model.user.User
import com.example.domain.model.user.UserId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional

class UserRepositoryImplKotestTest : StringSpec({
    // Test data
    val userId = 123L
    val name = "Test User"
    val email = "test@example.com"
    val loginId = "testuser"
    val password = "password123"
    val createdAt = Instant.now().minus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS)
    val createdBy = "creator"
    val updatedAt = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS)
    val updatedBy = "updater"
    
    // Create domain user
    val domainUserId = UserId.of(userId)
    val auditInfo = AuditInfo.of(createdAt, createdBy, updatedAt, updatedBy)
    val domainUser = User.reconstitute(
        id = domainUserId,
        name = name,
        email = email,
        loginId = loginId,
        password = password,
        auditInfo = auditInfo
    )
    
    // Create entity
    val userEntity = UserEntity(
        id = userId,
        name = name,
        email = email,
        loginId = loginId,
        password = password,
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        updatedBy = updatedBy
    )
    
    "save should convert domain User to entity and return converted domain User" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        val entitySlot = slot<UserEntity>()
        
        every { jpaUserRepository.save(capture(entitySlot)) } returns userEntity
        
        // When
        val result = userRepositoryImpl.save(domainUser)
        
        // Then
        verify { jpaUserRepository.save(any()) }
        
        val capturedEntity = entitySlot.captured
        capturedEntity.name shouldBe domainUser.name
        capturedEntity.email shouldBe domainUser.email
        capturedEntity.loginId shouldBe domainUser.loginId
        capturedEntity.password shouldBe domainUser.getPassword()
        
        result.id?.value shouldBe userId
        result.name shouldBe name
        result.email shouldBe email
        result.loginId shouldBe loginId
        result.getPassword() shouldBe password
    }
    
    "findById should return domain User when entity exists" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        every { jpaUserRepository.findById(userId) } returns Optional.of(userEntity)
        
        // When
        val result = userRepositoryImpl.findById(domainUserId)
        
        // Then
        verify { jpaUserRepository.findById(userId) }
        
        result shouldNotBe null
        result?.id?.value shouldBe userId
        result?.name shouldBe name
        result?.email shouldBe email
        result?.loginId shouldBe loginId
        result?.getPassword() shouldBe password
    }
    
    "findById should return null when entity does not exist" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        every { jpaUserRepository.findById(userId) } returns Optional.empty()
        
        // When
        val result = userRepositoryImpl.findById(domainUserId)
        
        // Then
        verify { jpaUserRepository.findById(userId) }
        
        result shouldBe null
    }
    
    "findByLoginId should return domain User when entity exists" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        every { jpaUserRepository.findByLoginId(loginId) } returns userEntity
        
        // When
        val result = userRepositoryImpl.findByLoginId(loginId)
        
        // Then
        verify { jpaUserRepository.findByLoginId(loginId) }
        
        result shouldNotBe null
        result?.id?.value shouldBe userId
        result?.name shouldBe name
        result?.email shouldBe email
        result?.loginId shouldBe loginId
        result?.getPassword() shouldBe password
    }
    
    "findByLoginId should return null when entity does not exist" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        every { jpaUserRepository.findByLoginId(loginId) } returns null
        
        // When
        val result = userRepositoryImpl.findByLoginId(loginId)
        
        // Then
        verify { jpaUserRepository.findByLoginId(loginId) }
        
        result shouldBe null
    }
    
    "findByEmail should return domain User when entity exists" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        every { jpaUserRepository.findByEmail(email) } returns userEntity
        
        // When
        val result = userRepositoryImpl.findByEmail(email)
        
        // Then
        verify { jpaUserRepository.findByEmail(email) }
        
        result shouldNotBe null
        result?.id?.value shouldBe userId
        result?.name shouldBe name
        result?.email shouldBe email
        result?.loginId shouldBe loginId
        result?.getPassword() shouldBe password
    }
    
    "findByEmail should return null when entity does not exist" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        every { jpaUserRepository.findByEmail(email) } returns null
        
        // When
        val result = userRepositoryImpl.findByEmail(email)
        
        // Then
        verify { jpaUserRepository.findByEmail(email) }
        
        result shouldBe null
    }
    
    "findAll should return list of domain Users" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        val userEntity2 = UserEntity(
            id = 456L,
            name = "Another User",
            email = "another@example.com",
            loginId = "anotheruser",
            password = "anotherpassword",
            createdAt = createdAt,
            createdBy = createdBy,
            updatedAt = updatedAt,
            updatedBy = updatedBy
        )
        
        every { jpaUserRepository.findAll() } returns listOf(userEntity, userEntity2)
        
        // When
        val result = userRepositoryImpl.findAll()
        
        // Then
        verify { jpaUserRepository.findAll() }
        
        result.size shouldBe 2
        result[0].id?.value shouldBe userId
        result[1].id?.value shouldBe 456L
    }
    
    "findAll should return empty list when no entities exist" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        every { jpaUserRepository.findAll() } returns emptyList()
        
        // When
        val result = userRepositoryImpl.findAll()
        
        // Then
        verify { jpaUserRepository.findAll() }
        
        result.size shouldBe 0
    }
    
    "delete should call deleteById with user ID" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        every { jpaUserRepository.deleteById(userId) } returns Unit
        
        // When
        userRepositoryImpl.delete(domainUser)
        
        // Then
        verify { jpaUserRepository.deleteById(userId) }
    }
    
    "delete should not call deleteById when user ID is null" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        val userWithoutId = User.create(
            name = name,
            email = email,
            loginId = loginId,
            password = password,
            createdBy = createdBy
        )
        
        // When
        userRepositoryImpl.delete(userWithoutId)
        
        // Then
        verify(exactly = 0) { jpaUserRepository.deleteById(any()) }
    }
    
    "deleteById should return true when user exists" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        every { jpaUserRepository.existsById(userId) } returns true
        every { jpaUserRepository.deleteById(userId) } returns Unit
        
        // When
        val result = userRepositoryImpl.deleteById(domainUserId)
        
        // Then
        verify { jpaUserRepository.existsById(userId) }
        verify { jpaUserRepository.deleteById(userId) }
        
        result shouldBe true
    }
    
    "deleteById should return false when user does not exist" {
        // Given
        val jpaUserRepository = mockk<JpaUserRepository>()
        val userRepositoryImpl = UserRepositoryImpl(jpaUserRepository)
        
        every { jpaUserRepository.existsById(userId) } returns false
        
        // When
        val result = userRepositoryImpl.deleteById(domainUserId)
        
        // Then
        verify { jpaUserRepository.existsById(userId) }
        verify(exactly = 0) { jpaUserRepository.deleteById(any()) }
        
        result shouldBe false
    }
})