package com.example.domain.model.order

/**
 * Value object representing a unique identifier for an Order.
 */
@JvmInline
value class OrderId private constructor(val value: Long) {
    companion object {
        /**
         * Creates an OrderId with the given value.
         */
        fun of(value: Long): OrderId {
            require(value > 0) { "Order ID must be positive" }
            return OrderId(value)
        }
    }

    override fun toString(): String = value.toString()
}