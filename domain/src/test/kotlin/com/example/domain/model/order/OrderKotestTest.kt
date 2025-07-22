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
    val validCustomerId = UserId.of(1L)
    val validCreator = "test-user"
    val testContext = DefaultDomainContext.user(userId = validCreator, userName = "Test User", roleId = "USER")
    
    fun createTestLineItems(): List<OrderLineItem> {
        return listOf(
            OrderLineItem.create(
                productId = ProductId.of(1L),
                productName = "Product 1",
                quantity = 2,
                unitPrice = BigDecimal("30.00")
            ),
            OrderLineItem.create(
                productId = ProductId.of(2L),
                productName = "Product 2",
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
            context = testContext,
            customerId = validCustomerId,
            lineItems = lineItems
        )
        
        // Then
        order.id shouldBe null
        order.customerId shouldBe validCustomerId
        order.getTotalAmount() shouldBe BigDecimal("100.00") // 2*30 + 1*40 = 100
        order.getStatus() shouldBe OrderStatus.PENDING
        order.getAuditInfo().createdBy shouldBe testContext.userId
        order.getAuditInfo().updatedBy shouldBe testContext.userId
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
                context = testContext,
                customerId = validCustomerId,
                lineItems = emptyList()
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
            context = testContext,
            customerId = validCustomerId,
            lineItems = lineItems
        )
        
        // Then
        order.getTotalAmount() shouldBe BigDecimal("107.00") // 3*25.50 + 2*15.25 = 76.50 + 30.50 = 107.00
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
        order.getTotalAmount() shouldBe totalAmount
        order.getAuditInfo() shouldBe auditInfo
        order.getLineItems().size shouldBe 2
        order.getDomainEvents().size shouldBe 0 // No events for reconstituted orders
    }
    
    "should transition from PENDING to PAID" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val updaterContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        
        // When
        order.pay(updaterContext)
        
        // Then
        order.getStatus() shouldBe OrderStatus.PAID
        order.getAuditInfo().updatedBy shouldBe updaterContext.userId
        order.getLineItems().size shouldBe 2
        
        // Check domain events - should have OrderCreated + OrderPaid
        val events = order.getDomainEvents()
        events.size shouldBe 2
        events[0].shouldBeInstanceOf<OrderCreated>()
        events[1].shouldBeInstanceOf<OrderPaid>()
    }
    
    "should throw exception when paying an order that is not PENDING" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val adminContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        order.pay(adminContext)
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.pay(adminContext)
        }
    }
    
    "should transition from PAID to SHIPPED" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val adminContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        order.pay(adminContext)
        val shipperContext = DefaultDomainContext.user(userId = "shipper", userName = "Shipper", roleId = "SHIPPER")
        
        // When
        order.ship(shipperContext)
        
        // Then
        order.getStatus() shouldBe OrderStatus.SHIPPED
        order.getAuditInfo().updatedBy shouldBe shipperContext.userId
        
        // Check domain events - should have OrderCreated + OrderPaid + OrderShipped
        val events = order.getDomainEvents()
        events.size shouldBe 3
        events[0].shouldBeInstanceOf<OrderCreated>()
        events[1].shouldBeInstanceOf<OrderPaid>()
        events[2].shouldBeInstanceOf<OrderShipped>()
    }
    
    "should throw exception when shipping an order that is not PAID" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val shipperContext = DefaultDomainContext.user(userId = "shipper", userName = "Shipper", roleId = "SHIPPER")
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.ship(shipperContext)
        }
    }
    
    "should transition from SHIPPED to DELIVERED" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val adminContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        order.pay(adminContext)
        val shipperContext = DefaultDomainContext.user(userId = "shipper", userName = "Shipper", roleId = "SHIPPER")
        order.ship(shipperContext)
        val delivererContext = DefaultDomainContext.user(userId = "deliverer", userName = "Deliverer", roleId = "DELIVERER")
        
        // When
        order.deliver(delivererContext)
        
        // Then
        order.getStatus() shouldBe OrderStatus.DELIVERED
        order.getAuditInfo().updatedBy shouldBe delivererContext.userId
        
        // Check domain events - should have OrderCreated + OrderPaid + OrderShipped + OrderDelivered
        val events = order.getDomainEvents()
        events.size shouldBe 4
        events[0].shouldBeInstanceOf<OrderCreated>()
        events[1].shouldBeInstanceOf<OrderPaid>()
        events[2].shouldBeInstanceOf<OrderShipped>()
        events[3].shouldBeInstanceOf<OrderDelivered>()
    }
    
    "should throw exception when delivering an order that is not SHIPPED" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val delivererContext = DefaultDomainContext.user(userId = "deliverer", userName = "Deliverer", roleId = "DELIVERER")
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.deliver(delivererContext)
        }
    }
    
    "should cancel PENDING order" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val adminContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        
        // When
        order.cancel(adminContext)
        
        // Then
        order.getStatus() shouldBe OrderStatus.CANCELLED
        order.getAuditInfo().updatedBy shouldBe adminContext.userId
        
        // Check domain events - should have OrderCreated + OrderCancelled
        val events = order.getDomainEvents()
        events.size shouldBe 2
        events[0].shouldBeInstanceOf<OrderCreated>()
        events[1].shouldBeInstanceOf<OrderCancelled>()
    }
    
    "should cancel PAID order" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val adminContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        order.pay(adminContext)
        val cancellerContext = DefaultDomainContext.user(userId = "canceller", userName = "Canceller", roleId = "ADMIN")
        
        // When
        order.cancel(cancellerContext)
        
        // Then
        order.getStatus() shouldBe OrderStatus.CANCELLED
        order.getAuditInfo().updatedBy shouldBe cancellerContext.userId
        
        // Check domain events - should have OrderCreated + OrderPaid + OrderCancelled
        val events = order.getDomainEvents()
        events.size shouldBe 3
        events[0].shouldBeInstanceOf<OrderCreated>()
        events[1].shouldBeInstanceOf<OrderPaid>()
        events[2].shouldBeInstanceOf<OrderCancelled>()
    }
    
    "should throw exception when cancelling SHIPPED order" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val adminContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        order.pay(adminContext)
        val shipperContext = DefaultDomainContext.user(userId = "shipper", userName = "Shipper", roleId = "SHIPPER")
        order.ship(shipperContext)
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.cancel(adminContext)
        }
    }
    
    "should throw exception when cancelling DELIVERED order" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val adminContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        order.pay(adminContext)
        val shipperContext = DefaultDomainContext.user(userId = "shipper", userName = "Shipper", roleId = "SHIPPER")
        order.ship(shipperContext)
        val delivererContext = DefaultDomainContext.user(userId = "deliverer", userName = "Deliverer", roleId = "DELIVERER")
        order.deliver(delivererContext)
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.cancel(adminContext)
        }
    }
    
    "should add line item to PENDING order" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createSingleLineItem()
        )
        val newLineItem = OrderLineItem.create(
            productId = ProductId.of(2L),
            productName = "New Product",
            quantity = 1,
            unitPrice = BigDecimal("50.00")
        )
        val updaterContext = DefaultDomainContext.user(userId = "updater", userName = "Updater", roleId = "USER")
        
        // When
        order.addLineItem(updaterContext, newLineItem)
        
        // Then
        order.getLineItems().size shouldBe 2
        order.getTotalAmount() shouldBe BigDecimal("150.00") // 100 + 50
        order.getAuditInfo().updatedBy shouldBe updaterContext.userId
        
        // Check domain events - should still have only OrderCreated (addLineItem doesn't generate events)
        val events = order.getDomainEvents()
        events.size shouldBe 1
        events[0].shouldBeInstanceOf<OrderCreated>()
    }
    
    "should throw exception when adding line item to non-PENDING order" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val adminContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        order.pay(adminContext)
        val newLineItem = OrderLineItem.create(
            productId = ProductId.of(3L),
            productName = "New Product",
            quantity = 1,
            unitPrice = BigDecimal("50.00")
        )
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.addLineItem(adminContext, newLineItem)
        }
    }
    
    "should remove line item from PENDING order" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val updaterContext = DefaultDomainContext.user(userId = "updater", userName = "Updater", roleId = "USER")
        
        // When
        order.removeLineItem(updaterContext, ProductId.of(1L))
        
        // Then
        order.getLineItems().size shouldBe 1
        order.getTotalAmount() shouldBe BigDecimal("40.00") // Only Product 2 remains
        order.getAuditInfo().updatedBy shouldBe updaterContext.userId
        
        // Check domain events - should still have only OrderCreated (removeLineItem doesn't generate events)
        val events = order.getDomainEvents()
        events.size shouldBe 1
        events[0].shouldBeInstanceOf<OrderCreated>()
    }
    
    "should throw exception when removing line item from non-PENDING order" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val adminContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        order.pay(adminContext)
        
        // When & Then
        shouldThrow<IllegalStateException> {
            order.removeLineItem(adminContext, ProductId.of(1L))
        }
    }
    
    "should update line item in PENDING order" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val updatedLineItem = OrderLineItem.create(
            productId = ProductId.of(1L),
            productName = "Updated Product 1",
            quantity = 3,
            unitPrice = BigDecimal("25.00")
        )
        val updaterContext = DefaultDomainContext.user(userId = "updater", userName = "Updater", roleId = "USER")
        
        // When
        order.updateLineItem(updaterContext, updatedLineItem)
        
        // Then
        order.getLineItems().size shouldBe 2
        order.getTotalAmount() shouldBe BigDecimal("115.00") // 3*25 + 1*40 = 75 + 40 = 115
        order.getAuditInfo().updatedBy shouldBe updaterContext.userId
        
        // Check domain events - should still have only OrderCreated (updateLineItem doesn't generate events)
        val events = order.getDomainEvents()
        events.size shouldBe 1
        events[0].shouldBeInstanceOf<OrderCreated>()
    }
    
    "should clear domain events" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        
        // When
        order.clearDomainEvents()
        
        // Then
        order.getDomainEvents().size shouldBe 0
    }
    
    "should accumulate domain events across state changes" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createTestLineItems()
        )
        val adminContext = DefaultDomainContext.user(userId = "admin", userName = "Admin", roleId = "ADMIN")
        val shipperContext = DefaultDomainContext.user(userId = "shipper", userName = "Shipper", roleId = "SHIPPER")
        val delivererContext = DefaultDomainContext.user(userId = "deliverer", userName = "Deliverer", roleId = "DELIVERER")
        
        // When - perform multiple state changes
        order.pay(adminContext)
        order.ship(shipperContext)
        order.deliver(delivererContext)
        
        // Then - all events should be accumulated
        order.getStatus() shouldBe OrderStatus.DELIVERED
        order.getAuditInfo().updatedBy shouldBe delivererContext.userId
        
        val events = order.getDomainEvents()
        events.size shouldBe 4
        events[0].shouldBeInstanceOf<OrderCreated>()
        events[1].shouldBeInstanceOf<OrderPaid>()
        events[2].shouldBeInstanceOf<OrderShipped>()
        events[3].shouldBeInstanceOf<OrderDelivered>()
    }
    
    "should preserve domain events when modifying line items" {
        // Given
        val order = Order.create(
            context = testContext,
            customerId = validCustomerId,
            lineItems = createSingleLineItem()
        )
        val newLineItem = OrderLineItem.create(
            productId = ProductId.of(2L),
            productName = "New Product",
            quantity = 1,
            unitPrice = BigDecimal("50.00")
        )
        val updaterContext = DefaultDomainContext.user(userId = "updater", userName = "Updater", roleId = "USER")
        
        // When - add line item then pay
        order.addLineItem(updaterContext, newLineItem)
        order.pay(updaterContext)
        
        // Then - both events should be preserved
        val events = order.getDomainEvents()
        events.size shouldBe 2
        events[0].shouldBeInstanceOf<OrderCreated>()
        events[1].shouldBeInstanceOf<OrderPaid>()
        
        order.getTotalAmount() shouldBe BigDecimal("150.00") // 100 + 50
        order.getStatus() shouldBe OrderStatus.PAID
    }
})