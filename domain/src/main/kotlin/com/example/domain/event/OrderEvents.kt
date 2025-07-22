package com.example.domain.event

import com.example.domain.common.DomainContext
import com.example.domain.model.order.OrderId
import com.example.domain.model.order.OrderStatus
import com.example.domain.model.user.UserId
import java.math.BigDecimal
import java.time.Instant

/**
 * Event payload for order-related events.
 */
data class OrderEventPayload(
    val orderId: OrderId?,
    val customerId: UserId,
    val orderDate: Instant,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val lineItemCount: Int
)

/**
 * Domain event fired when a new order is created.
 */
class OrderCreated(
    override val context: DomainContext,
    override val payload: OrderEventPayload
) : DomainEventBase<OrderEventPayload>()

/**
 * Domain event fired when an order is paid.
 */
class OrderPaid(
    override val context: DomainContext,
    override val payload: OrderEventPayload
) : DomainEventBase<OrderEventPayload>()

/**
 * Domain event fired when an order is shipped.
 */
class OrderShipped(
    override val context: DomainContext,
    override val payload: OrderEventPayload
) : DomainEventBase<OrderEventPayload>()

/**
 * Domain event fired when an order is delivered.
 */
class OrderDelivered(
    override val context: DomainContext,
    override val payload: OrderEventPayload
) : DomainEventBase<OrderEventPayload>()

/**
 * Domain event fired when an order is cancelled.
 */
class OrderCancelled(
    override val context: DomainContext,
    override val payload: OrderEventPayload
) : DomainEventBase<OrderEventPayload>()