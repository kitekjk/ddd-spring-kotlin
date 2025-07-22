package com.example.domain.model.order

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow

class ProductIdKotestTest : StringSpec({
    
    "should create ProductId with valid positive value" {
        // When
        val productId = ProductId.of(1L)
        
        // Then
        productId.value shouldBe 1L
        productId.toString() shouldBe "1"
    }
    
    "should create ProductId with large positive value" {
        // When
        val productId = ProductId.of(Long.MAX_VALUE)
        
        // Then
        productId.value shouldBe Long.MAX_VALUE
        productId.toString() shouldBe Long.MAX_VALUE.toString()
    }
    
    "should throw exception when creating ProductId with zero value" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            ProductId.of(0L)
        }.message shouldBe "Product ID must be positive"
    }
    
    "should throw exception when creating ProductId with negative value" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            ProductId.of(-1L)
        }.message shouldBe "Product ID must be positive"
    }
    
    "should throw exception when creating ProductId with large negative value" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            ProductId.of(Long.MIN_VALUE)
        }.message shouldBe "Product ID must be positive"
    }
    
    "should implement value equality" {
        // Given
        val productId1 = ProductId.of(123L)
        val productId2 = ProductId.of(123L)
        val productId3 = ProductId.of(456L)
        
        // Then
        (productId1 == productId2) shouldBe true
        (productId1 == productId3) shouldBe false
        productId1.hashCode() shouldBe productId2.hashCode()
    }
    
    "should have meaningful toString representation" {
        // Given
        val productId = ProductId.of(12345L)
        
        // When
        val stringRepresentation = productId.toString()
        
        // Then
        stringRepresentation shouldBe "12345"
    }
    
    "should be usable as map key" {
        // Given
        val productId1 = ProductId.of(1L)
        val productId2 = ProductId.of(2L)
        val productId3 = ProductId.of(1L) // Same value as productId1
        
        val map = mutableMapOf<ProductId, String>()
        
        // When
        map[productId1] = "Product 1"
        map[productId2] = "Product 2"
        map[productId3] = "Updated Product 1" // Should overwrite productId1
        
        // Then
        map.size shouldBe 2
        map[productId1] shouldBe "Updated Product 1"
        map[productId2] shouldBe "Product 2"
        map[productId3] shouldBe "Updated Product 1"
    }
    
    "should be comparable by value" {
        // Given
        val productId1 = ProductId.of(100L)
        val productId2 = ProductId.of(200L)
        val productId3 = ProductId.of(100L)
        
        // Then
        (productId1.value < productId2.value) shouldBe true
        (productId2.value > productId1.value) shouldBe true
        (productId1.value == productId3.value) shouldBe true
    }
    
    "should maintain immutability" {
        // Given
        val originalValue = 42L
        val productId = ProductId.of(originalValue)
        
        // When
        val retrievedValue = productId.value
        
        // Then
        retrievedValue shouldBe originalValue
        // Value class is immutable by design, no mutation methods available
    }
    
    "should work with collections" {
        // Given
        val productIds = listOf(
            ProductId.of(1L),
            ProductId.of(2L),
            ProductId.of(3L),
            ProductId.of(1L) // Duplicate
        )
        
        // When
        val uniqueProductIds = productIds.toSet()
        val sortedValues = productIds.map { it.value }.sorted()
        
        // Then
        uniqueProductIds.size shouldBe 3 // Duplicates removed
        sortedValues shouldBe listOf(1L, 1L, 2L, 3L)
        uniqueProductIds.contains(ProductId.of(1L)) shouldBe true
        uniqueProductIds.contains(ProductId.of(4L)) shouldBe false
    }
})