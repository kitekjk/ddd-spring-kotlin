package com.example.domain.model.order

import com.example.domain.common.DomainContext
import com.example.domain.event.*
import com.example.domain.model.common.AuditInfo
import com.example.domain.model.user.UserId
import java.math.BigDecimal
import java.time.Instant

/**
 * Order aggregate root representing an order in the system.
 * Contains order details and manages order state transitions.
 */
class Order private constructor(
    val id: OrderId?,
    val customerId: UserId,
    val orderDate: Instant,
    private var status: OrderStatus,
    val totalAmount: BigDecimal,
    val auditInfo: AuditInfo,
    private val lineItems: MutableList<OrderLineItem> = mutableListOf(),
    private val domainEvents: MutableList<DomainEvent<*>> = mutableListOf()
) {
    companion object {
        /**
         * Creates a new Order instance.
         */
        fun create(
            context: DomainContext,
            customerId: UserId,
            lineItems: List<OrderLineItem>
        ): Order {
            require(lineItems.isNotEmpty()) { "Order must have at least one line item" }
            
            val totalAmount = lineItems.sumOf { it.getTotalPrice() }
            require(totalAmount > BigDecimal.ZERO) { "Total amount must be positive" }

            val order = Order(
                id = null,
                customerId = customerId,
                orderDate = Instant.now(),
                status = OrderStatus.PENDING,
                totalAmount = totalAmount,
                auditInfo = AuditInfo.newInstance(context.userId),
                lineItems = lineItems.toMutableList()
            )
            
            // Add OrderCreated event
            order.addDomainEvent(OrderCreated(context, order.createEventPayload()))
            
            return order
        }

        /**
         * Reconstitutes an Order from persistence.
         */
        fun reconstitute(
            id: OrderId,
            customerId: UserId,
            orderDate: Instant,
            status: OrderStatus,
            totalAmount: BigDecimal,
            auditInfo: AuditInfo,
            lineItems: List<OrderLineItem> = emptyList()
        ): Order {
            return Order(
                id = id,
                customerId = customerId,
                orderDate = orderDate,
                status = status,
                totalAmount = totalAmount,
                auditInfo = auditInfo,
                lineItems = lineItems.toMutableList()
            )
        }
    }

    /**
     * Gets the current status of the order.
     */
    fun getStatus(): OrderStatus = status

    /**
     * Gets the line items as an immutable list.
     */
    fun getLineItems(): List<OrderLineItem> = lineItems.toList()

    /**
     * Gets the domain events as an immutable list.
     */
    fun getDomainEvents(): List<DomainEvent<*>> = domainEvents.toList()

    /**
     * Adds a domain event to the aggregate.
     */
    private fun addDomainEvent(event: DomainEvent<*>) {
        domainEvents.add(event)
    }

    /**
     * Clears all domain events from the aggregate.
     * This should be called after events have been published.
     */
    fun clearDomainEvents() {
        domainEvents.clear()
    }

    /**
     * Creates an event payload from the current order state.
     */
    private fun createEventPayload(): OrderEventPayload {
        return OrderEventPayload(
            orderId = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = this.status,
            totalAmount = this.totalAmount,
            lineItemCount = this.lineItems.size
        )
    }

    /**
     * Adds a line item to the order.
     * Can only add items when order is in PENDING status.
     */
    fun addLineItem(context: DomainContext, lineItem: OrderLineItem): Order {
        if (status != OrderStatus.PENDING) {
            throw IllegalStateException("Cannot add line items to order in ${status.name} status. Only PENDING orders can be modified.")
        }

        val newLineItems = this.lineItems.toMutableList()
        newLineItems.add(lineItem)
        val newTotalAmount = newLineItems.sumOf { it.getTotalPrice() }

        return Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = this.status,
            totalAmount = newTotalAmount,
            auditInfo = this.auditInfo.update(context.userId),
            lineItems = newLineItems
        )
    }

    /**
     * Removes a line item from the order.
     * Can only remove items when order is in PENDING status.
     */
    fun removeLineItem(context: DomainContext, productId: ProductId): Order {
        if (status != OrderStatus.PENDING) {
            throw IllegalStateException("Cannot remove line items from order in ${status.name} status. Only PENDING orders can be modified.")
        }

        val newLineItems = this.lineItems.toMutableList()
        newLineItems.removeIf { it.productId == productId }
        
        if (newLineItems.isEmpty()) {
            throw IllegalStateException("Order must have at least one line item")
        }

        val newTotalAmount = newLineItems.sumOf { it.getTotalPrice() }

        return Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = this.status,
            totalAmount = newTotalAmount,
            auditInfo = this.auditInfo.update(context.userId),
            lineItems = newLineItems
        )
    }

    /**
     * Updates a line item in the order.
     * Can only update items when order is in PENDING status.
     */
    fun updateLineItem(context: DomainContext, updatedLineItem: OrderLineItem): Order {
        if (status != OrderStatus.PENDING) {
            throw IllegalStateException("Cannot update line items in order in ${status.name} status. Only PENDING orders can be modified.")
        }

        val newLineItems = this.lineItems.toMutableList()
        val index = newLineItems.indexOfFirst { it.productId == updatedLineItem.productId }
        
        if (index == -1) {
            throw IllegalArgumentException("Line item with product ID ${updatedLineItem.productId} not found in order")
        }

        newLineItems[index] = updatedLineItem
        val newTotalAmount = newLineItems.sumOf { it.getTotalPrice() }

        return Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = this.status,
            totalAmount = newTotalAmount,
            auditInfo = this.auditInfo.update(context.userId),
            lineItems = newLineItems
        )
    }

    /**
     * Marks the order as paid.
     * Can only transition from PENDING status.
     */
    fun pay(context: DomainContext): Order {
        validateStateTransition(OrderStatus.PENDING, OrderStatus.PAID)
        
        val paidOrder = Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = OrderStatus.PAID,
            totalAmount = this.totalAmount,
            auditInfo = this.auditInfo.update(context.userId),
            lineItems = this.lineItems.toMutableList()
        )
        
        // Add OrderPaid event
        paidOrder.addDomainEvent(OrderPaid(context, paidOrder.createEventPayload()))
        
        return paidOrder
    }

    /**
     * Marks the order as shipped.
     * Can only transition from PAID status.
     */
    fun ship(context: DomainContext): Order {
        validateStateTransition(OrderStatus.PAID, OrderStatus.SHIPPED)
        
        val shippedOrder = Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = OrderStatus.SHIPPED,
            totalAmount = this.totalAmount,
            auditInfo = this.auditInfo.update(context.userId),
            lineItems = this.lineItems.toMutableList()
        )
        
        // Add OrderShipped event
        shippedOrder.addDomainEvent(OrderShipped(context, shippedOrder.createEventPayload()))
        
        return shippedOrder
    }

    /**
     * Marks the order as delivered.
     * Can only transition from SHIPPED status.
     */
    fun deliver(context: DomainContext): Order {
        validateStateTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED)
        
        val deliveredOrder = Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = OrderStatus.DELIVERED,
            totalAmount = this.totalAmount,
            auditInfo = this.auditInfo.update(context.userId),
            lineItems = this.lineItems.toMutableList()
        )
        
        // Add OrderDelivered event
        deliveredOrder.addDomainEvent(OrderDelivered(context, deliveredOrder.createEventPayload()))
        
        return deliveredOrder
    }

    /**
     * Cancels the order.
     * Can only cancel orders that are in PENDING or PAID status.
     */
    fun cancel(context: DomainContext): Order {
        if (status != OrderStatus.PENDING && status != OrderStatus.PAID) {
            throw IllegalStateException("Cannot cancel order in ${status.name} status. Only PENDING or PAID orders can be cancelled.")
        }
        
        val cancelledOrder = Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = OrderStatus.CANCELLED,
            totalAmount = this.totalAmount,
            auditInfo = this.auditInfo.update(context.userId),
            lineItems = this.lineItems.toMutableList()
        )
        
        // Add OrderCancelled event
        cancelledOrder.addDomainEvent(OrderCancelled(context, cancelledOrder.createEventPayload()))
        
        return cancelledOrder
    }

    /**
     * Validates that the current status matches the expected status for a state transition.
     */
    private fun validateStateTransition(expectedStatus: OrderStatus, newStatus: OrderStatus) {
        if (status != expectedStatus) {
            throw IllegalStateException("Cannot transition from ${status.name} to ${newStatus.name}. Order must be in ${expectedStatus.name} status.")
        }
    }
}