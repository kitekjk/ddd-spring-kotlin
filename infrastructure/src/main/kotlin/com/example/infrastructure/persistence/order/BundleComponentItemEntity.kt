package com.example.infrastructure.persistence.order

import com.example.domain.model.order.BundleComponentItem
import com.example.domain.model.order.ProductId
import jakarta.persistence.*

/**
 * JPA entity for BundleComponentItem.
 */
@Entity
@Table(
    name = "bundle_component_items",
    indexes = [
        Index(name = "idx_bundle_component_product_id", columnList = "component_product_id"),
        Index(name = "idx_bundle_component_line_item_id", columnList = "order_line_item_id")
    ]
)
class BundleComponentItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "component_product_id", nullable = false)
    var componentProductId: Long,

    @Column(name = "component_name", nullable = false, length = 255)
    var componentName: String,

    @Column(nullable = false)
    var quantity: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_line_item_id", nullable = false)
    var orderLineItem: OrderLineItemEntity
) {
    /**
     * Converts this JPA entity to a domain BundleComponentItem.
     */
    fun toDomain(): BundleComponentItem {
        return BundleComponentItem.reconstitute(
            componentProductId = ProductId.of(componentProductId),
            componentName = componentName,
            quantity = quantity
        )
    }

    companion object {
        /**
         * Creates a BundleComponentItemEntity from a domain BundleComponentItem.
         */
        fun fromDomain(
            bundleComponent: BundleComponentItem,
            orderLineItem: OrderLineItemEntity
        ): BundleComponentItemEntity {
            return BundleComponentItemEntity(
                componentProductId = bundleComponent.componentProductId.value,
                componentName = bundleComponent.componentName,
                quantity = bundleComponent.getQuantity(),
                orderLineItem = orderLineItem
            )
        }
    }
}