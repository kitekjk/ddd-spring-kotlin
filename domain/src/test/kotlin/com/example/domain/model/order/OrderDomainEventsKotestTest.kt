package com.example.domain.model.order

import com.example.domain.common.DefaultDomainContext
import com.example.domain.event.OrderCreated
import com.example.domain.event.OrderPaid
import com.example.domain.event.OrderShipped
import com.example.domain.model.user.UserId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.math.BigDecimal

/**
 * Test class to verify that domain events are properly accumulated and preserved
 * across Order state changes and line item modifications.
 * 
 * This addresses the issue where domain events were being lost when Order functions
 * created new instances instead of modifying the existing instance.
 */
class OrderDomainEventsKotestTest : StringSpec({
    val testContext = DefaultDomainContext.user(
        userId = "test-user", 
        userName = "Test User", 
        roleId = "admin"
    )
    val customerId = UserId.of(123L)
    
    "should preserve domain events across line item modifications and state changes" {
        // Given
        val lineItem1 = OrderLineItem.create(
            productId = ProductId.of(1L),
            productName = "Product 1",
            quantity = 2,
            unitPrice = BigDecimal("100.00")
        )
        
        val lineItem2 = OrderLineItem.create(
            productId = ProductId.of(2L),
            productName = "Product 2",
            quantity = 1,
            unitPrice = BigDecimal("50.00")
        )
        
        // When - Create order
        val order = Order.create(testContext, customerId, listOf(lineItem1))
        
        // Then - Initial state
        order.getDomainEvents().size shouldBe 1
        order.getDomainEvents()[0].shouldBeInstanceOf<OrderCreated>()
        
        // When - Add line item (should preserve existing events)
        order.addLineItem(testContext, lineItem2)
        
        // Then - Events should be preserved
        order.getDomainEvents().size shouldBe 1 // addLineItem doesn't generate events
        order.getDomainEvents()[0].shouldBeInstanceOf<OrderCreated>()
        order.getTotalAmount() shouldBe BigDecimal("250.00") // 2*100 + 1*50 = 250
        
        // When - Pay order (should preserve existing events and add new one)
        order.pay(testContext)
        
        // Then - Events should be accumulated
        val eventsAfterPay = order.getDomainEvents()
        eventsAfterPay.size shouldBe 2
        eventsAfterPay[0].shouldBeInstanceOf<OrderCreated>()
        eventsAfterPay[1].shouldBeInstanceOf<OrderPaid>()
        order.getStatus() shouldBe OrderStatus.PAID
        
        // When - Ship order (should preserve existing events and add new one)
        order.ship(testContext)
        
        // Then - All events should be accumulated
        val eventsAfterShip = order.getDomainEvents()
        eventsAfterShip.size shouldBe 3
        eventsAfterShip[0].shouldBeInstanceOf<OrderCreated>()
        eventsAfterShip[1].shouldBeInstanceOf<OrderPaid>()
        eventsAfterShip[2].shouldBeInstanceOf<OrderShipped>()
        order.getStatus() shouldBe OrderStatus.SHIPPED
    }
    
    "should accumulate domain events through complete order lifecycle" {
        // Given
        val lineItems = listOf(
            OrderLineItem.create(
                productId = ProductId.of(1L),
                productName = "Test Product",
                quantity = 1,
                unitPrice = BigDecimal("100.00")
            )
        )
        
        // When - Create and process order through complete lifecycle
        val order = Order.create(testContext, customerId, lineItems)
        order.pay(testContext)
        order.ship(testContext)
        order.deliver(testContext)
        
        // Then - All domain events should be preserved
        val events = order.getDomainEvents()
        events.size shouldBe 4
        events[0].shouldBeInstanceOf<OrderCreated>()
        events[1].shouldBeInstanceOf<OrderPaid>()
        events[2].shouldBeInstanceOf<OrderShipped>()
        events[3].shouldBeInstanceOf<com.example.domain.event.OrderDelivered>()
        
        order.getStatus() shouldBe OrderStatus.DELIVERED
    }
    
    "should preserve domain events when order is cancelled after payment" {
        // Given
        val lineItems = listOf(
            OrderLineItem.create(
                productId = ProductId.of(1L),
                productName = "Test Product",
                quantity = 1,
                unitPrice = BigDecimal("100.00")
            )
        )
        
        // When - Create, pay, then cancel order
        val order = Order.create(testContext, customerId, lineItems)
        order.pay(testContext)
        order.cancel(testContext)
        
        // Then - All domain events should be preserved
        val events = order.getDomainEvents()
        events.size shouldBe 3
        events[0].shouldBeInstanceOf<OrderCreated>()
        events[1].shouldBeInstanceOf<OrderPaid>()
        events[2].shouldBeInstanceOf<com.example.domain.event.OrderCancelled>()
        
        order.getStatus() shouldBe OrderStatus.CANCELLED
    }
    
    "should preserve domain events when modifying line items before payment" {
        // Given
        val initialLineItem = OrderLineItem.create(
            productId = ProductId.of(1L),
            productName = "Initial Product",
            quantity = 1,
            unitPrice = BigDecimal("50.00")
        )
        
        val additionalLineItem = OrderLineItem.create(
            productId = ProductId.of(2L),
            productName = "Additional Product",
            quantity = 2,
            unitPrice = BigDecimal("30.00")
        )
        
        val updatedLineItem = OrderLineItem.create(
            productId = ProductId.of(1L),
            productName = "Updated Product",
            quantity = 3,
            unitPrice = BigDecimal("40.00")
        )
        
        // When - Create order and modify line items
        val order = Order.create(testContext, customerId, listOf(initialLineItem))
        order.addLineItem(testContext, additionalLineItem)
        order.updateLineItem(testContext, updatedLineItem)
        order.pay(testContext)
        
        // Then - Domain events should be preserved throughout modifications
        val events = order.getDomainEvents()
        events.size shouldBe 2 // OrderCreated + OrderPaid (line item modifications don't generate events)
        events[0].shouldBeInstanceOf<OrderCreated>()
        events[1].shouldBeInstanceOf<OrderPaid>()
        
        // Verify final state
        order.getStatus() shouldBe OrderStatus.PAID
        order.getTotalAmount() shouldBe BigDecimal("180.00") // 3*40 + 2*30 = 120 + 60 = 180
        order.getLineItems().size shouldBe 2
    }
    
    "should clear domain events when explicitly requested" {
        // Given
        val lineItems = listOf(
            OrderLineItem.create(
                productId = ProductId.of(1L),
                productName = "Test Product",
                quantity = 1,
                unitPrice = BigDecimal("100.00")
            )
        )
        
        val order = Order.create(testContext, customerId, lineItems)
        order.pay(testContext)
        
        // Verify events exist
        order.getDomainEvents().size shouldBe 2
        
        // When - Clear domain events
        order.clearDomainEvents()
        
        // Then - Events should be cleared
        order.getDomainEvents().size shouldBe 0
        
        // But order state should remain unchanged
        order.getStatus() shouldBe OrderStatus.PAID
        order.getTotalAmount() shouldBe BigDecimal("100.00")
    }
})