package com.example.domain.model.order

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow

class OrderStatusKotestTest : StringSpec({
    "should have correct name for PENDING status" {
        // When & Then
        OrderStatus.PENDING.name shouldBe "PENDING"
    }
    
    "should have correct name for PAID status" {
        // When & Then
        OrderStatus.PAID.name shouldBe "PAID"
    }
    
    "should have correct name for SHIPPED status" {
        // When & Then
        OrderStatus.SHIPPED.name shouldBe "SHIPPED"
    }
    
    "should have correct name for DELIVERED status" {
        // When & Then
        OrderStatus.DELIVERED.name shouldBe "DELIVERED"
    }
    
    "should have correct name for CANCELLED status" {
        // When & Then
        OrderStatus.CANCELLED.name shouldBe "CANCELLED"
    }
    
    "should convert to string representation" {
        // When & Then
        OrderStatus.PENDING.toString() shouldBe "PENDING"
        OrderStatus.PAID.toString() shouldBe "PAID"
        OrderStatus.SHIPPED.toString() shouldBe "SHIPPED"
        OrderStatus.DELIVERED.toString() shouldBe "DELIVERED"
        OrderStatus.CANCELLED.toString() shouldBe "CANCELLED"
    }
    
    "should create OrderStatus from string" {
        // When & Then
        OrderStatus.fromString("PENDING") shouldBe OrderStatus.PENDING
        OrderStatus.fromString("PAID") shouldBe OrderStatus.PAID
        OrderStatus.fromString("SHIPPED") shouldBe OrderStatus.SHIPPED
        OrderStatus.fromString("DELIVERED") shouldBe OrderStatus.DELIVERED
        OrderStatus.fromString("CANCELLED") shouldBe OrderStatus.CANCELLED
    }
    
    "should create OrderStatus from lowercase string" {
        // When & Then
        OrderStatus.fromString("pending") shouldBe OrderStatus.PENDING
        OrderStatus.fromString("paid") shouldBe OrderStatus.PAID
    }
    
    "should throw exception for invalid status string" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            OrderStatus.fromString("INVALID_STATUS")
        }
    }
})