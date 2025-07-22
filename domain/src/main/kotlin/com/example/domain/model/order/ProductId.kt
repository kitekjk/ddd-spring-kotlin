package com.example.domain.model.order

/**
 * Value object representing a unique identifier for a Product.
 */
@JvmInline
value class ProductId private constructor(val value: Long) {
    companion object {
        /**
         * Creates a ProductId with the given value.
         */
        fun of(value: Long): ProductId {
            require(value > 0) { "Product ID must be positive" }
            return ProductId(value)
        }
    }

    override fun toString(): String = value.toString()
}