package com.example.domain.model.order

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
    val auditInfo: AuditInfo
) {
    companion object {
        /**
         * Creates a new Order instance.
         */
        fun create(
            customerId: UserId,
            totalAmount: BigDecimal,
            createdBy: String
        ): Order {
            require(totalAmount > BigDecimal.ZERO) { "Total amount must be positive" }

            return Order(
                id = null,
                customerId = customerId,
                orderDate = Instant.now(),
                status = OrderStatus.PENDING,
                totalAmount = totalAmount,
                auditInfo = AuditInfo.newInstance(createdBy)
            )
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
            auditInfo: AuditInfo
        ): Order {
            return Order(
                id = id,
                customerId = customerId,
                orderDate = orderDate,
                status = status,
                totalAmount = totalAmount,
                auditInfo = auditInfo
            )
        }
    }

    /**
     * Gets the current status of the order.
     */
    fun getStatus(): OrderStatus = status

    /**
     * Marks the order as paid.
     * Can only transition from PENDING status.
     */
    fun pay(updatedBy: String): Order {
        validateStateTransition(OrderStatus.PENDING, OrderStatus.PAID)
        
        return Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = OrderStatus.PAID,
            totalAmount = this.totalAmount,
            auditInfo = this.auditInfo.update(updatedBy)
        )
    }

    /**
     * Marks the order as shipped.
     * Can only transition from PAID status.
     */
    fun ship(updatedBy: String): Order {
        validateStateTransition(OrderStatus.PAID, OrderStatus.SHIPPED)
        
        return Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = OrderStatus.SHIPPED,
            totalAmount = this.totalAmount,
            auditInfo = this.auditInfo.update(updatedBy)
        )
    }

    /**
     * Marks the order as delivered.
     * Can only transition from SHIPPED status.
     */
    fun deliver(updatedBy: String): Order {
        validateStateTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED)
        
        return Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = OrderStatus.DELIVERED,
            totalAmount = this.totalAmount,
            auditInfo = this.auditInfo.update(updatedBy)
        )
    }

    /**
     * Cancels the order.
     * Can only cancel orders that are in PENDING or PAID status.
     */
    fun cancel(updatedBy: String): Order {
        if (status != OrderStatus.PENDING && status != OrderStatus.PAID) {
            throw IllegalStateException("Cannot cancel order in ${status.name} status. Only PENDING or PAID orders can be cancelled.")
        }
        
        return Order(
            id = this.id,
            customerId = this.customerId,
            orderDate = this.orderDate,
            status = OrderStatus.CANCELLED,
            totalAmount = this.totalAmount,
            auditInfo = this.auditInfo.update(updatedBy)
        )
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