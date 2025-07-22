package com.example.infrastructure.persistence.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for OrderEntity.
 */
@Repository
interface JpaOrderRepository : JpaRepository<OrderEntity, Long> {
    
    /**
     * Finds orders by customer ID.
     */
    fun findByCustomerId(customerId: Long): List<OrderEntity>
    
    /**
     * Finds orders by status.
     */
    fun findByStatus(status: String): List<OrderEntity>
    
    /**
     * Finds orders by customer ID and status.
     */
    fun findByCustomerIdAndStatus(customerId: Long, status: String): List<OrderEntity>
    
    /**
     * Finds orders with their line items eagerly loaded.
     */
    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.lineItems WHERE o.id = :orderId")
    fun findByIdWithLineItems(@Param("orderId") orderId: Long): OrderEntity?
    
    /**
     * Finds orders by customer ID with line items eagerly loaded.
     */
    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.lineItems WHERE o.customerId = :customerId")
    fun findByCustomerIdWithLineItems(@Param("customerId") customerId: Long): List<OrderEntity>
}