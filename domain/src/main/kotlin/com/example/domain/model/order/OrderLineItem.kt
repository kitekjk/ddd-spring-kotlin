package com.example.domain.model.order

import java.math.BigDecimal

/**
 * Entity representing a line item within an Order.
 * Contains product information, quantity, and pricing details.
 */
class OrderLineItem private constructor(
    val productId: ProductId,
    val productName: String,
    private var quantity: Int,
    val unitPrice: BigDecimal,
    private val bundleComponents: MutableList<BundleComponentItem> = mutableListOf()
) {
    companion object {
        /**
         * Creates a new OrderLineItem instance.
         */
        fun create(
            productId: ProductId,
            productName: String,
            quantity: Int,
            unitPrice: BigDecimal
        ): OrderLineItem {
            require(productName.isNotBlank()) { "Product name cannot be blank" }
            require(quantity > 0) { "Quantity must be positive" }
            require(unitPrice >= BigDecimal.ZERO) { "Unit price cannot be negative" }

            return OrderLineItem(
                productId = productId,
                productName = productName,
                quantity = quantity,
                unitPrice = unitPrice
            )
        }

        /**
         * Reconstitutes an OrderLineItem from persistence.
         */
        fun reconstitute(
            productId: ProductId,
            productName: String,
            quantity: Int,
            unitPrice: BigDecimal,
            bundleComponents: List<BundleComponentItem> = emptyList()
        ): OrderLineItem {
            return OrderLineItem(
                productId = productId,
                productName = productName,
                quantity = quantity,
                unitPrice = unitPrice,
                bundleComponents = bundleComponents.toMutableList()
            )
        }
    }

    /**
     * Gets the current quantity.
     */
    fun getQuantity(): Int = quantity

    /**
     * Gets the bundle components as an immutable list.
     */
    fun getBundleComponents(): List<BundleComponentItem> = bundleComponents.toList()

    /**
     * Calculates the total price for this line item.
     */
    fun getTotalPrice(): BigDecimal = unitPrice.multiply(BigDecimal.valueOf(quantity.toLong()))

    /**
     * Updates the quantity of this line item.
     */
    fun updateQuantity(newQuantity: Int): OrderLineItem {
        require(newQuantity > 0) { "Quantity must be positive" }
        
        return OrderLineItem(
            productId = this.productId,
            productName = this.productName,
            quantity = newQuantity,
            unitPrice = this.unitPrice,
            bundleComponents = this.bundleComponents.toMutableList()
        )
    }

    /**
     * Adds a bundle component to this line item.
     */
    fun addBundleComponent(component: BundleComponentItem): OrderLineItem {
        val newComponents = this.bundleComponents.toMutableList()
        newComponents.add(component)
        
        return OrderLineItem(
            productId = this.productId,
            productName = this.productName,
            quantity = this.quantity,
            unitPrice = this.unitPrice,
            bundleComponents = newComponents
        )
    }

    /**
     * Removes a bundle component from this line item.
     */
    fun removeBundleComponent(componentProductId: ProductId): OrderLineItem {
        val newComponents = this.bundleComponents.toMutableList()
        newComponents.removeIf { it.componentProductId == componentProductId }
        
        return OrderLineItem(
            productId = this.productId,
            productName = this.productName,
            quantity = this.quantity,
            unitPrice = this.unitPrice,
            bundleComponents = newComponents
        )
    }

    /**
     * Checks if this line item contains bundle components.
     */
    fun hasBundleComponents(): Boolean = bundleComponents.isNotEmpty()

    /**
     * Checks if this line item contains a specific component product.
     */
    fun containsComponent(componentProductId: ProductId): Boolean {
        return bundleComponents.any { it.componentProductId == componentProductId }
    }

    /**
     * Gets the total quantity of a specific component product.
     */
    fun getComponentQuantity(componentProductId: ProductId): Int {
        return bundleComponents
            .filter { it.componentProductId == componentProductId }
            .sumOf { it.getQuantity() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderLineItem) return false
        return productId == other.productId
    }

    override fun hashCode(): Int {
        return productId.hashCode()
    }

    override fun toString(): String {
        return "OrderLineItem(productId=$productId, productName='$productName', quantity=$quantity, unitPrice=$unitPrice)"
    }
}