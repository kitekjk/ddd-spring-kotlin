package com.example.infrastructure.persistence.order

import com.example.domain.model.common.AuditInfo
import com.example.domain.model.order.*
import com.example.domain.model.user.UserId
import com.example.infrastructure.persistence.common.AuditInfoEmbeddable
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

/**
 * JPA entity for Order aggregate root.
 */
@Entity
@Table(
    name = "orders",
    indexes = [
        Index(name = "idx_orders_customer_id", columnList = "customer_id"),
        Index(name = "idx_orders_status", columnList = "status"),
        Index(name = "idx_orders_order_date", columnList = "order_date")
    ]
)
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "customer_id", nullable = false)
    var customerId: Long,

    @Column(name = "order_date", nullable = false)
    var orderDate: Instant,

    @Column(nullable = false, length = 20)
    var status: String,

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    var totalAmount: BigDecimal,

    @Embedded
    var auditInfo: AuditInfoEmbeddable,

    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var lineItems: MutableList<OrderLineItemEntity> = mutableListOf()
) {
    /**
     * Converts this JPA entity to a domain Order.
     */
    fun toDomain(): Order {
        val orderId = id?.let { OrderId.of(it) }
        val domainAuditInfo = auditInfo.toDomain()

        val domainLineItems = lineItems.map { it.toDomain() }

        return if (orderId != null) {
            Order.reconstitute(
                id = orderId,
                customerId = UserId.of(customerId),
                orderDate = orderDate,
                status = OrderStatus.fromString(status),
                totalAmount = totalAmount,
                auditInfo = domainAuditInfo,
                lineItems = domainLineItems
            )
        } else {
            throw IllegalStateException("Cannot convert OrderEntity to Order: ID is null")
        }
    }

    /**
     * Updates this entity from a domain Order.
     */
    fun updateFromDomain(order: Order) {
        this.customerId = order.customerId.value
        this.orderDate = order.orderDate
        this.status = order.getStatus().name
        this.totalAmount = order.getTotalAmount()
        this.auditInfo.updateFromDomain(order.getAuditInfo())

        // Update line items
        this.lineItems.clear()
        order.getLineItems().forEach { domainLineItem ->
            val lineItemEntity = OrderLineItemEntity.fromDomain(domainLineItem, this)
            this.lineItems.add(lineItemEntity)
        }
    }

    companion object {
        /**
         * Creates an OrderEntity from a domain Order.
         */
        fun fromDomain(order: Order): OrderEntity {
            val entity = OrderEntity(
                id = order.id?.value,
                customerId = order.customerId.value,
                orderDate = order.orderDate,
                status = order.getStatus().name,
                totalAmount = order.getTotalAmount(),
                auditInfo = AuditInfoEmbeddable.fromDomain(order.getAuditInfo())
            )

            // Add line items
            order.getLineItems().forEach { domainLineItem ->
                val lineItemEntity = OrderLineItemEntity.fromDomain(domainLineItem, entity)
                entity.lineItems.add(lineItemEntity)
            }

            return entity
        }
    }
}