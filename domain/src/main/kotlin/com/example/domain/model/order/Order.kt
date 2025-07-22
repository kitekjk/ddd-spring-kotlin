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
    private var totalAmount: BigDecimal,
    private var auditInfo: AuditInfo,
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
     * Gets the total amount of the order.
     */
    fun getTotalAmount(): BigDecimal = totalAmount

    /**
     * Gets the audit information of the order.
     */
    fun getAuditInfo(): AuditInfo = auditInfo

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
    fun addLineItem(context: DomainContext, lineItem: OrderLineItem) {
        if (status != OrderStatus.PENDING) {
            throw IllegalStateException("Cannot add line items to order in ${status.name} status. Only PENDING orders can be modified.")
        }

        this.lineItems.add(lineItem)
        this.totalAmount = this.lineItems.sumOf { it.getTotalPrice() }
        this.auditInfo = this.auditInfo.update(context.userId)
    }

    /**
     * Removes a line item from the order.
     * Can only remove items when order is in PENDING status.
     */
    fun removeLineItem(context: DomainContext, productId: ProductId) {
        if (status != OrderStatus.PENDING) {
            throw IllegalStateException("Cannot remove line items from order in ${status.name} status. Only PENDING orders can be modified.")
        }

        this.lineItems.removeIf { it.productId == productId }
        
        if (this.lineItems.isEmpty()) {
            throw IllegalStateException("Order must have at least one line item")
        }

        this.totalAmount = this.lineItems.sumOf { it.getTotalPrice() }
        this.auditInfo = this.auditInfo.update(context.userId)
    }

    /**
     * Updates a line item in the order.
     * Can only update items when order is in PENDING status.
     */
    fun updateLineItem(context: DomainContext, updatedLineItem: OrderLineItem) {
        if (status != OrderStatus.PENDING) {
            throw IllegalStateException("Cannot update line items in order in ${status.name} status. Only PENDING orders can be modified.")
        }

        val index = this.lineItems.indexOfFirst { it.productId == updatedLineItem.productId }
        
        if (index == -1) {
            throw IllegalArgumentException("Line item with product ID ${updatedLineItem.productId} not found in order")
        }

        this.lineItems[index] = updatedLineItem
        this.totalAmount = this.lineItems.sumOf { it.getTotalPrice() }
        this.auditInfo = this.auditInfo.update(context.userId)
    }

    /**
     * Marks the order as paid.
     * Can only transition from PENDING status.
     */
    fun pay(context: DomainContext) {
        validateStateTransition(OrderStatus.PENDING, OrderStatus.PAID)
        
        this.status = OrderStatus.PAID
        this.auditInfo = this.auditInfo.update(context.userId)
        
        // Add OrderPaid event
        this.addDomainEvent(OrderPaid(context, this.createEventPayload()))
    }

    /**
     * Marks the order as shipped.
     * Can only transition from PAID status.
     */
    fun ship(context: DomainContext) {
        validateStateTransition(OrderStatus.PAID, OrderStatus.SHIPPED)
        
        this.status = OrderStatus.SHIPPED
        this.auditInfo = this.auditInfo.update(context.userId)
        
        // Add OrderShipped event
        this.addDomainEvent(OrderShipped(context, this.createEventPayload()))
    }

    /**
     * Marks the order as delivered.
     * Can only transition from SHIPPED status.
     */
    fun deliver(context: DomainContext) {
        validateStateTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED)
        
        this.status = OrderStatus.DELIVERED
        this.auditInfo = this.auditInfo.update(context.userId)
        
        // Add OrderDelivered event
        this.addDomainEvent(OrderDelivered(context, this.createEventPayload()))
    }

    /**
     * Cancels the order.
     * Can only cancel orders that are in PENDING or PAID status.
     */
    fun cancel(context: DomainContext) {
        if (status != OrderStatus.PENDING && status != OrderStatus.PAID) {
            throw IllegalStateException("Cannot cancel order in ${status.name} status. Only PENDING or PAID orders can be cancelled.")
        }
        
        this.status = OrderStatus.CANCELLED
        this.auditInfo = this.auditInfo.update(context.userId)
        
        // Add OrderCancelled event
        this.addDomainEvent(OrderCancelled(context, this.createEventPayload()))
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