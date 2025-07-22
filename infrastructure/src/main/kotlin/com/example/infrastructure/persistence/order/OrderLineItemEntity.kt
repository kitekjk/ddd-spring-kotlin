package com.example.infrastructure.persistence.order

import com.example.domain.model.order.OrderLineItem
import com.example.domain.model.order.ProductId
import jakarta.persistence.*
import java.math.BigDecimal

/**
 * JPA entity for OrderLineItem.
 */
@Entity
@Table(
    name = "order_line_items",
    indexes = [
        Index(name = "idx_order_line_item_product_id", columnList = "product_id"),
        Index(name = "idx_order_line_item_order_id", columnList = "order_id")
    ]
)
class OrderLineItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "product_id", nullable = false)
    var productId: Long,

    @Column(name = "product_name", nullable = false, length = 255)
    var productName: String,

    @Column(nullable = false)
    var quantity: Int,

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    var unitPrice: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderEntity,

    @OneToMany(
        mappedBy = "orderLineItem",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var bundleComponents: MutableList<BundleComponentItemEntity> = mutableListOf()
) {
    /**
     * Converts this JPA entity to a domain OrderLineItem.
     */
    fun toDomain(): OrderLineItem {
        val domainBundleComponents = bundleComponents.map { it.toDomain() }
        
        return OrderLineItem.reconstitute(
            productId = ProductId.of(productId),
            productName = productName,
            quantity = quantity,
            unitPrice = unitPrice,
            bundleComponents = domainBundleComponents
        )
    }

    /**
     * Updates this entity from a domain OrderLineItem.
     */
    fun updateFromDomain(orderLineItem: OrderLineItem) {
        this.productId = orderLineItem.productId.value
        this.productName = orderLineItem.productName
        this.quantity = orderLineItem.getQuantity()
        this.unitPrice = orderLineItem.unitPrice

        // Update bundle components
        this.bundleComponents.clear()
        orderLineItem.getBundleComponents().forEach { domainBundleComponent ->
            val bundleComponentEntity = BundleComponentItemEntity.fromDomain(domainBundleComponent, this)
            this.bundleComponents.add(bundleComponentEntity)
        }
    }

    companion object {
        /**
         * Creates an OrderLineItemEntity from a domain OrderLineItem.
         */
        fun fromDomain(
            orderLineItem: OrderLineItem,
            order: OrderEntity
        ): OrderLineItemEntity {
            val entity = OrderLineItemEntity(
                productId = orderLineItem.productId.value,
                productName = orderLineItem.productName,
                quantity = orderLineItem.getQuantity(),
                unitPrice = orderLineItem.unitPrice,
                order = order
            )

            // Add bundle components
            orderLineItem.getBundleComponents().forEach { domainBundleComponent ->
                val bundleComponentEntity = BundleComponentItemEntity.fromDomain(domainBundleComponent, entity)
                entity.bundleComponents.add(bundleComponentEntity)
            }

            return entity
        }
    }
}