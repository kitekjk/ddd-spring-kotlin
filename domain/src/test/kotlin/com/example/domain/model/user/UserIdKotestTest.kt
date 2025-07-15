package com.example.domain.model.user

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow

class UserIdKotestTest : StringSpec({
    "should create UserId with positive value" {
        // Given
        val value = 123L
        
        // When
        val userId = UserId.of(value)
        
        // Then
        userId.value shouldBe value
    }
    
    "should throw exception when creating UserId with zero value" {
        // Given
        val value = 0L
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            UserId.of(value)
        }
    }
    
    "should throw exception when creating UserId with negative value" {
        // Given
        val value = -1L
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            UserId.of(value)
        }
    }
    
    "should convert to string representation" {
        // Given
        val value = 123L
        val userId = UserId.of(value)
        
        // When
        val result = userId.toString()
        
        // Then
        result shouldBe "123"
    }
})