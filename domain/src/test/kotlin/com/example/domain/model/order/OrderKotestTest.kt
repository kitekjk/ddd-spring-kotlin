package com.example.domain.model.order

import com.example.domain.model.common.AuditInfo
import com.example.domain.model.user.UserId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import java.math.BigDecimal
import java.time.Instant

class OrderKotestTest : StringSpec({
    // Test constants
    val validCustomerId = UserId.of(1L)
    val validTotalAmount = BigDecimal("100.00")
    val validCreator = "system"
    
    "should create order with valid inputs" {
        // When
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        
        // Then
        order.id shouldBe null
        order.customerId shouldBe validCustomerId
        order.totalAmount shouldBe validTotalAmount
        order.getStatus() shouldBe OrderStatus.PENDING
        order.auditInfo.createdBy shouldBe validCreator
        order.auditInfo.updatedBy shouldBe validCreator
    }
    
    "should throw exception when creating order with zero total amount" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Order.create(
                customerId = validCustomerId,
                totalAmount = BigDecimal.ZERO,
                createdBy = validCreator
            )
        }
    }
    
    "should throw exception when creating order with negative total amount" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Order.create(
                customerId = validCustomerId,
                totalAmount = BigDecimal("-10.00"),
                createdBy = validCreator
            )
        }
    }
    
    "should reconstitute order from persistence" {
        // Given
        val orderId = OrderId.of(1L)
        val orderDate = Instant.now()
        val status = OrderStatus.PENDING
        val now = Instant.now()
        val auditInfo = AuditInfo.of(now, validCreator, now, validCreator)
        
        // When
        val order = Order.reconstitute(
            id = orderId,
            customerId = validCustomerId,
            orderDate = orderDate,
            status = status,
            totalAmount = validTotalAmount,
            auditInfo = auditInfo
        )
        
        // Then
        order.id shouldBe orderId
        order.customerId shouldBe validCustomerId
        order.orderDate shouldBe orderDate
        order.getStatus() shouldBe status
        order.totalAmount shouldBe validTotalAmount
        order.auditInfo shouldBe auditInfo
    }
    
    "should transition from PENDING to PAID" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        val updater = "admin"
        
        // When
        val paidOrder = order.pay(updater)
        
        // Then
        paidOrder.getStatus() shouldBe OrderStatus.PAID
        paidOrder.auditInfo.updatedBy shouldBe updater
    }
    
    "should throw exception when paying an order that is not PENDING" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        val paidOrder = order.pay("admin")
        
        // When & Then
        shouldThrow<IllegalStateException> {
            paidOrder.pay("admin")
        }
    }
    
    "should transition from PAID to SHIPPED" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        val paidOrder = order.pay("admin")
        val updater = "shipper"
        
        // When
        val shippedOrder = paidOrder.ship(updater)
        
        // Then
        shippedOrder.getStatus() shouldBe OrderStatus.SHIPPED
        shippedOrder.auditInfo.updatedBy shouldBe updater
    }
    
    "should throw exception when shipping an order that is not PAID" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.ship("shipper")
        }
    }
    
    "should transition from SHIPPED to DELIVERED" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        val paidOrder = order.pay("admin")
        val shippedOrder = paidOrder.ship("shipper")
        val updater = "deliverer"
        
        // When
        val deliveredOrder = shippedOrder.deliver(updater)
        
        // Then
        deliveredOrder.getStatus() shouldBe OrderStatus.DELIVERED
        deliveredOrder.auditInfo.updatedBy shouldBe updater
    }
    
    "should throw exception when delivering an order that is not SHIPPED" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        val paidOrder = order.pay("admin")
        
        // When & Then
        shouldThrow<IllegalStateException> {
            paidOrder.deliver("deliverer")
        }
    }
    
    "should cancel a PENDING order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        val updater = "canceller"
        
        // When
        val cancelledOrder = order.cancel(updater)
        
        // Then
        cancelledOrder.getStatus() shouldBe OrderStatus.CANCELLED
        cancelledOrder.auditInfo.updatedBy shouldBe updater
    }
    
    "should cancel a PAID order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        val paidOrder = order.pay("admin")
        val updater = "canceller"
        
        // When
        val cancelledOrder = paidOrder.cancel(updater)
        
        // Then
        cancelledOrder.getStatus() shouldBe OrderStatus.CANCELLED
        cancelledOrder.auditInfo.updatedBy shouldBe updater
    }
    
    "should throw exception when cancelling a SHIPPED order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        val paidOrder = order.pay("admin")
        val shippedOrder = paidOrder.ship("shipper")
        
        // When & Then
        shouldThrow<IllegalStateException> {
            shippedOrder.cancel("canceller")
        }
    }
    
    "should throw exception when cancelling a DELIVERED order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            totalAmount = validTotalAmount,
            createdBy = validCreator
        )
        val paidOrder = order.pay("admin")
        val shippedOrder = paidOrder.ship("shipper")
        val deliveredOrder = shippedOrder.deliver("deliverer")
        
        // When & Then
        shouldThrow<IllegalStateException> {
            deliveredOrder.cancel("canceller")
        }
    }
})