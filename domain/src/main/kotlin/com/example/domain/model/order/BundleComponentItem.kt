package com.example.domain.model.order

/**
 * Entity representing a component item within a bundle product.
 * Contains information about individual components that make up a bundle.
 */
class BundleComponentItem private constructor(
    val componentProductId: ProductId,
    val componentName: String,
    private var quantity: Int
) {
    companion object {
        /**
         * Creates a new BundleComponentItem instance.
         */
        fun create(
            componentProductId: ProductId,
            componentName: String,
            quantity: Int
        ): BundleComponentItem {
            require(componentName.isNotBlank()) { "Component name cannot be blank" }
            require(quantity > 0) { "Quantity must be positive" }

            return BundleComponentItem(
                componentProductId = componentProductId,
                componentName = componentName,
                quantity = quantity
            )
        }

        /**
         * Reconstitutes a BundleComponentItem from persistence.
         */
        fun reconstitute(
            componentProductId: ProductId,
            componentName: String,
            quantity: Int
        ): BundleComponentItem {
            return BundleComponentItem(
                componentProductId = componentProductId,
                componentName = componentName,
                quantity = quantity
            )
        }
    }

    /**
     * Gets the current quantity.
     */
    fun getQuantity(): Int = quantity

    /**
     * Updates the quantity of this component.
     */
    fun updateQuantity(newQuantity: Int): BundleComponentItem {
        require(newQuantity > 0) { "Quantity must be positive" }
        
        return BundleComponentItem(
            componentProductId = this.componentProductId,
            componentName = this.componentName,
            quantity = newQuantity
        )
    }

    /**
     * Checks if this component represents the same product as another component.
     */
    fun isSameProduct(other: BundleComponentItem): Boolean {
        return this.componentProductId == other.componentProductId
    }

    /**
     * Checks if this component represents a specific product.
     */
    fun isProduct(productId: ProductId): Boolean {
        return this.componentProductId == productId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BundleComponentItem) return false
        return componentProductId == other.componentProductId
    }

    override fun hashCode(): Int {
        return componentProductId.hashCode()
    }

    override fun toString(): String {
        return "BundleComponentItem(componentProductId=$componentProductId, componentName='$componentName', quantity=$quantity)"
    }
}