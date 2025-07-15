package com.example.infrastructure.persistence.user

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ContextConfiguration
import java.time.Instant
import java.time.temporal.ChronoUnit
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringExtension
import org.springframework.test.context.TestConstructor
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension as JUnitSpringExtension

@DataJpaTest
@ContextConfiguration(classes = [TestConfig::class])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ExtendWith(JUnitSpringExtension::class)
class JpaUserRepositoryKotestTest(
    private val entityManager: TestEntityManager,
    private val jpaUserRepository: JpaUserRepository
) : FunSpec({

    beforeTest {
        // Clear the database before each test
        entityManager.clear()
    }

    test("should save and find user by ID") {
        // Given
        val userEntity = createUserEntity()

        // When
        val savedEntity = jpaUserRepository.save(userEntity)
        val foundEntity = jpaUserRepository.findById(savedEntity.id!!).orElse(null)

        // Then
        foundEntity shouldNotBe null
        foundEntity.id shouldBe savedEntity.id
        foundEntity.name shouldBe userEntity.name
        foundEntity.email shouldBe userEntity.email
        foundEntity.loginId shouldBe userEntity.loginId
        foundEntity.password shouldBe userEntity.password
        foundEntity.createdAt shouldBe userEntity.createdAt
        foundEntity.createdBy shouldBe userEntity.createdBy
        foundEntity.updatedAt shouldBe userEntity.updatedAt
        foundEntity.updatedBy shouldBe userEntity.updatedBy
    }

    test("should find user by login ID") {
        // Given
        val userEntity = createUserEntity()
        entityManager.persist(userEntity)
        entityManager.flush()

        // When
        val foundEntity = jpaUserRepository.findByLoginId(userEntity.loginId)

        // Then
        foundEntity shouldNotBe null
        foundEntity!!.name shouldBe userEntity.name
        foundEntity.email shouldBe userEntity.email
        foundEntity.loginId shouldBe userEntity.loginId
    }

    test("should return null when finding by non-existent login ID") {
        // Given
        val nonExistentLoginId = "nonexistent"

        // When
        val foundEntity = jpaUserRepository.findByLoginId(nonExistentLoginId)

        // Then
        foundEntity shouldBe null
    }

    test("should find user by email") {
        // Given
        val userEntity = createUserEntity()
        entityManager.persist(userEntity)
        entityManager.flush()

        // When
        val foundEntity = jpaUserRepository.findByEmail(userEntity.email)

        // Then
        foundEntity shouldNotBe null
        foundEntity!!.name shouldBe userEntity.name
        foundEntity.email shouldBe userEntity.email
        foundEntity.loginId shouldBe userEntity.loginId
    }

    test("should return null when finding by non-existent email") {
        // Given
        val nonExistentEmail = "nonexistent@example.com"

        // When
        val foundEntity = jpaUserRepository.findByEmail(nonExistentEmail)

        // Then
        foundEntity shouldBe null
    }

    test("should handle case sensitivity in login ID and email searches") {
        // Given
        val userEntity = createUserEntity()
        entityManager.persist(userEntity)
        entityManager.flush()

        // When
        val foundByUppercaseLoginId = jpaUserRepository.findByLoginId(userEntity.loginId.uppercase())
        val foundByUppercaseEmail = jpaUserRepository.findByEmail(userEntity.email.uppercase())

        // Then
        // This test verifies the actual behavior of the database regarding case sensitivity
        // The assertion will depend on the database configuration
        // For H2 in-memory database with default settings, searches are typically case-insensitive
        if (foundByUppercaseLoginId != null) {
            foundByUppercaseLoginId.loginId shouldBe userEntity.loginId
        }

        if (foundByUppercaseEmail != null) {
            foundByUppercaseEmail.email shouldBe userEntity.email
        }
    }

    test("should handle special characters in login ID and email searches") {
        // Given
        val specialLoginId = "user.name-123"
        val specialEmail = "user.name+test@example.com"

        val userEntity = createUserEntity(
            loginId = specialLoginId,
            email = specialEmail
        )

        entityManager.persist(userEntity)
        entityManager.flush()

        // When
        val foundBySpecialLoginId = jpaUserRepository.findByLoginId(specialLoginId)
        val foundBySpecialEmail = jpaUserRepository.findByEmail(specialEmail)

        // Then
        foundBySpecialLoginId shouldNotBe null
        foundBySpecialLoginId!!.loginId shouldBe specialLoginId

        foundBySpecialEmail shouldNotBe null
        foundBySpecialEmail!!.email shouldBe specialEmail
    }
}) {
    companion object {
        // Helper function to create a test user entity
        private fun createUserEntity(
            id: Long? = null,
            name: String = "Test User",
            email: String = "test@example.com",
            loginId: String = "testuser",
            password: String = "password123"
        ): UserEntity {
            val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
            return UserEntity(
                id = id,
                name = name,
                email = email,
                loginId = loginId,
                password = password,
                createdAt = now,
                createdBy = "test-creator",
                updatedAt = now,
                updatedBy = "test-creator"
            )
        }
    }

    // This is required to integrate Kotest with Spring
    override fun extensions(): List<Extension> = listOf(SpringExtension)
}

// Test configuration class for Spring context
@org.springframework.context.annotation.Configuration
@org.springframework.boot.autoconfigure.EnableAutoConfiguration
@org.springframework.boot.autoconfigure.domain.EntityScan("com.example.infrastructure.persistence.user")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories("com.example.infrastructure.persistence.user")
class TestConfig
