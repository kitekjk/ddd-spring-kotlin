package com.example.domain.model.order

/**
 * Represents the possible states of an Order.
 * Implemented as a sealed class following the project guidelines.
 */
sealed class OrderStatus(val name: String) {
    /**
     * Order has been created but not yet paid.
     */
    object PENDING : OrderStatus("PENDING")

    /**
     * Order has been paid but not yet shipped.
     */
    object PAID : OrderStatus("PAID")

    /**
     * Order has been shipped but not yet delivered.
     */
    object SHIPPED : OrderStatus("SHIPPED")

    /**
     * Order has been delivered to the customer.
     */
    object DELIVERED : OrderStatus("DELIVERED")

    /**
     * Order has been cancelled.
     */
    object CANCELLED : OrderStatus("CANCELLED")

    companion object {
        /**
         * Returns the OrderStatus corresponding to the given name.
         */
        fun fromString(name: String): OrderStatus {
            return when (name.uppercase()) {
                "PENDING" -> PENDING
                "PAID" -> PAID
                "SHIPPED" -> SHIPPED
                "DELIVERED" -> DELIVERED
                "CANCELLED" -> CANCELLED
                else -> throw IllegalArgumentException("Unknown order status: $name")
            }
        }
    }

    override fun toString(): String = name
}