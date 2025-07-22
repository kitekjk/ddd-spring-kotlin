package com.example.domain.model.order

import com.example.domain.common.DefaultDomainContext
import com.example.domain.event.OrderCreated
import com.example.domain.event.OrderPaid
import com.example.domain.event.OrderShipped
import com.example.domain.event.OrderDelivered
import com.example.domain.event.OrderCancelled
import com.example.domain.model.common.AuditInfo
import com.example.domain.model.user.UserId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.assertions.throwables.shouldThrow
import java.math.BigDecimal
import java.time.Instant

class OrderKotestTest : StringSpec({
    // Test constants
    val validCustomerId = UserId.of(1L)
    val validCreator = "system"
    val testContext = DefaultDomainContext.system()
    
    // Helper function to create test line items
    fun createTestLineItems(): List<OrderLineItem> {
        return listOf(
            OrderLineItem.create(
                productId = ProductId.of(1L),
                productName = "Test Product 1",
                quantity = 2,
                unitPrice = BigDecimal("30.00")
            ),
            OrderLineItem.create(
                productId = ProductId.of(2L),
                productName = "Test Product 2",
                quantity = 1,
                unitPrice = BigDecimal("40.00")
            )
        )
    }
    
    fun createSingleLineItem(): List<OrderLineItem> {
        return listOf(
            OrderLineItem.create(
                productId = ProductId.of(1L),
                productName = "Single Product",
                quantity = 1,
                unitPrice = BigDecimal("100.00")
            )
        )
    }
    
    "should create order with valid inputs" {
        // Given
        val lineItems = createTestLineItems()
        
        // When
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = lineItems,
            createdBy = validCreator,
            context = testContext
        )
        
        // Then
        order.id shouldBe null
        order.customerId shouldBe validCustomerId
        order.totalAmount shouldBe BigDecimal("100.00") // 2*30 + 1*40 = 100
        order.getStatus() shouldBe OrderStatus.PENDING
        order.auditInfo.createdBy shouldBe validCreator
        order.auditInfo.updatedBy shouldBe validCreator
        order.getLineItems().size shouldBe 2
        
        // Check domain events
        val events = order.getDomainEvents()
        events.size shouldBe 1
        events[0].shouldBeInstanceOf<OrderCreated>()
    }
    
    "should throw exception when creating order with empty line items" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Order.create(
                customerId = validCustomerId,
                lineItems = emptyList(),
                createdBy = validCreator,
                context = testContext
            )
        }
    }
    
    "should calculate total amount from line items" {
        // Given
        val lineItems = listOf(
            OrderLineItem.create(
                productId = ProductId.of(1L),
                productName = "Product 1",
                quantity = 3,
                unitPrice = BigDecimal("25.50")
            ),
            OrderLineItem.create(
                productId = ProductId.of(2L),
                productName = "Product 2",
                quantity = 2,
                unitPrice = BigDecimal("15.25")
            )
        )
        
        // When
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = lineItems,
            createdBy = validCreator,
            context = testContext
        )
        
        // Then
        order.totalAmount shouldBe BigDecimal("107.00") // 3*25.50 + 2*15.25 = 76.50 + 30.50 = 107.00
    }
    
    "should reconstitute order from persistence" {
        // Given
        val orderId = OrderId.of(1L)
        val orderDate = Instant.now()
        val status = OrderStatus.PENDING
        val totalAmount = BigDecimal("100.00")
        val now = Instant.now()
        val auditInfo = AuditInfo.of(now, validCreator, now, validCreator)
        val lineItems = createTestLineItems()
        
        // When
        val order = Order.reconstitute(
            id = orderId,
            customerId = validCustomerId,
            orderDate = orderDate,
            status = status,
            totalAmount = totalAmount,
            auditInfo = auditInfo,
            lineItems = lineItems
        )
        
        // Then
        order.id shouldBe orderId
        order.customerId shouldBe validCustomerId
        order.orderDate shouldBe orderDate
        order.getStatus() shouldBe status
        order.totalAmount shouldBe totalAmount
        order.auditInfo shouldBe auditInfo
        order.getLineItems().size shouldBe 2
        order.getDomainEvents().size shouldBe 0 // No events for reconstituted orders
    }
    
    "should transition from PENDING to PAID" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        val updater = "admin"
        
        // When
        val paidOrder = order.pay(updater, testContext)
        
        // Then
        paidOrder.getStatus() shouldBe OrderStatus.PAID
        paidOrder.auditInfo.updatedBy shouldBe updater
        paidOrder.getLineItems().size shouldBe 2
        
        // Check domain events
        val events = paidOrder.getDomainEvents()
        events.size shouldBe 1
        events[0].shouldBeInstanceOf<OrderPaid>()
    }
    
    "should throw exception when paying an order that is not PENDING" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        val paidOrder = order.pay("admin", testContext)
        
        // When & Then
        shouldThrow<IllegalStateException> {
            paidOrder.pay("admin", testContext)
        }
    }
    
    "should transition from PAID to SHIPPED" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        val paidOrder = order.pay("admin", testContext)
        val updater = "shipper"
        
        // When
        val shippedOrder = paidOrder.ship(updater, testContext)
        
        // Then
        shippedOrder.getStatus() shouldBe OrderStatus.SHIPPED
        shippedOrder.auditInfo.updatedBy shouldBe updater
        
        // Check domain events
        val events = shippedOrder.getDomainEvents()
        events.size shouldBe 1
        events[0].shouldBeInstanceOf<OrderShipped>()
    }
    
    "should throw exception when shipping an order that is not PAID" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.ship("shipper", testContext)
        }
    }
    
    "should transition from SHIPPED to DELIVERED" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        val paidOrder = order.pay("admin", testContext)
        val shippedOrder = paidOrder.ship("shipper", testContext)
        val updater = "delivery"
        
        // When
        val deliveredOrder = shippedOrder.deliver(updater, testContext)
        
        // Then
        deliveredOrder.getStatus() shouldBe OrderStatus.DELIVERED
        deliveredOrder.auditInfo.updatedBy shouldBe updater
        
        // Check domain events
        val events = deliveredOrder.getDomainEvents()
        events.size shouldBe 1
        events[0].shouldBeInstanceOf<OrderDelivered>()
    }
    
    "should throw exception when delivering an order that is not SHIPPED" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.deliver("delivery", testContext)
        }
    }
    
    "should cancel PENDING order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        val updater = "admin"
        
        // When
        val cancelledOrder = order.cancel(updater, testContext)
        
        // Then
        cancelledOrder.getStatus() shouldBe OrderStatus.CANCELLED
        cancelledOrder.auditInfo.updatedBy shouldBe updater
        
        // Check domain events
        val events = cancelledOrder.getDomainEvents()
        events.size shouldBe 1
        events[0].shouldBeInstanceOf<OrderCancelled>()
    }
    
    "should cancel PAID order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        val paidOrder = order.pay("admin", testContext)
        val updater = "admin"
        
        // When
        val cancelledOrder = paidOrder.cancel(updater, testContext)
        
        // Then
        cancelledOrder.getStatus() shouldBe OrderStatus.CANCELLED
        cancelledOrder.auditInfo.updatedBy shouldBe updater
        
        // Check domain events
        val events = cancelledOrder.getDomainEvents()
        events.size shouldBe 1
        events[0].shouldBeInstanceOf<OrderCancelled>()
    }
    
    "should throw exception when cancelling SHIPPED order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        val paidOrder = order.pay("admin", testContext)
        val shippedOrder = paidOrder.ship("shipper", testContext)
        
        // When & Then
        shouldThrow<IllegalStateException> {
            shippedOrder.cancel("admin", testContext)
        }
    }
    
    "should throw exception when cancelling DELIVERED order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        val paidOrder = order.pay("admin", testContext)
        val shippedOrder = paidOrder.ship("shipper", testContext)
        val deliveredOrder = shippedOrder.deliver("delivery", testContext)
        
        // When & Then
        shouldThrow<IllegalStateException> {
            deliveredOrder.cancel("admin", testContext)
        }
    }
    
    "should add line item to PENDING order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createSingleLineItem(),
            createdBy = validCreator,
            context = testContext
        )
        val newLineItem = OrderLineItem.create(
            productId = ProductId.of(2L),
            productName = "New Product",
            quantity = 1,
            unitPrice = BigDecimal("50.00")
        )
        
        // When
        val updatedOrder = order.addLineItem(newLineItem, "admin")
        
        // Then
        updatedOrder.getLineItems().size shouldBe 2
        updatedOrder.totalAmount shouldBe BigDecimal("150.00") // 100 + 50
    }
    
    "should throw exception when adding line item to non-PENDING order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createSingleLineItem(),
            createdBy = validCreator,
            context = testContext
        )
        val paidOrder = order.pay("admin", testContext)
        val newLineItem = OrderLineItem.create(
            productId = ProductId.of(2L),
            productName = "New Product",
            quantity = 1,
            unitPrice = BigDecimal("50.00")
        )
        
        // When & Then
        shouldThrow<IllegalStateException> {
            paidOrder.addLineItem(newLineItem, "admin")
        }
    }
    
    "should remove line item from PENDING order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        
        // When
        val updatedOrder = order.removeLineItem(ProductId.of(1L), "admin")
        
        // Then
        updatedOrder.getLineItems().size shouldBe 1
        updatedOrder.totalAmount shouldBe BigDecimal("40.00") // Only Product 2 remains
    }
    
    "should throw exception when removing last line item" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createSingleLineItem(),
            createdBy = validCreator,
            context = testContext
        )
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.removeLineItem(ProductId.of(1L), "admin")
        }
    }
    
    "should update line item in PENDING order" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        val updatedLineItem = OrderLineItem.create(
            productId = ProductId.of(1L),
            productName = "Updated Product 1",
            quantity = 5,
            unitPrice = BigDecimal("20.00")
        )
        
        // When
        val updatedOrder = order.updateLineItem(updatedLineItem, "admin")
        
        // Then
        updatedOrder.getLineItems().size shouldBe 2
        updatedOrder.totalAmount shouldBe BigDecimal("140.00") // 5*20 + 1*40 = 100 + 40 = 140
        val lineItem = updatedOrder.getLineItems().find { it.productId == ProductId.of(1L) }
        lineItem?.getQuantity() shouldBe 5
        lineItem?.productName shouldBe "Updated Product 1"
    }
    
    "should clear domain events" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        
        // When
        order.clearDomainEvents()
        
        // Then
        order.getDomainEvents().size shouldBe 0
    }
    
    "should maintain domain events across state transitions" {
        // Given
        val order = Order.create(
            customerId = validCustomerId,
            lineItems = createTestLineItems(),
            createdBy = validCreator,
            context = testContext
        )
        
        // When
        val paidOrder = order.pay("admin", testContext)
        val shippedOrder = paidOrder.ship("shipper", testContext)
        
        // Then
        // Each state transition creates a new Order instance with its own event
        order.getDomainEvents().size shouldBe 1
        order.getDomainEvents()[0].shouldBeInstanceOf<OrderCreated>()
        
        paidOrder.getDomainEvents().size shouldBe 1
        paidOrder.getDomainEvents()[0].shouldBeInstanceOf<OrderPaid>()
        
        shippedOrder.getDomainEvents().size shouldBe 1
        shippedOrder.getDomainEvents()[0].shouldBeInstanceOf<OrderShipped>()
    }
})