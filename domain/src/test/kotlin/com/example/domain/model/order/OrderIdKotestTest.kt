package com.example.domain.model.order

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow

class OrderIdKotestTest : StringSpec({
    "should create OrderId with positive value" {
        // Given
        val value = 123L
        
        // When
        val orderId = OrderId.of(value)
        
        // Then
        orderId.value shouldBe value
    }
    
    "should throw exception when creating OrderId with zero value" {
        // Given
        val value = 0L
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            OrderId.of(value)
        }
    }
    
    "should throw exception when creating OrderId with negative value" {
        // Given
        val value = -1L
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            OrderId.of(value)
        }
    }
    
    "should convert to string representation" {
        // Given
        val value = 123L
        val orderId = OrderId.of(value)
        
        // When
        val result = orderId.toString()
        
        // Then
        result shouldBe "123"
    }
})