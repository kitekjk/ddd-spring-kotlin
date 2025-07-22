package com.example.infrastructure.persistence.order

import com.example.domain.model.order.Order
import com.example.domain.model.order.OrderId
import com.example.domain.model.order.OrderRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of OrderRepository using Spring Data JPA.
 */
@Repository
@Transactional
class OrderRepositoryImpl(
    private val jpaOrderRepository: JpaOrderRepository
) : OrderRepository {
    
    override fun save(order: Order): Order {
        val existingEntity = order.id?.let { orderId ->
            jpaOrderRepository.findByIdWithLineItems(orderId.value)
        }
        
        return if (existingEntity != null) {
            // Update existing entity
            existingEntity.updateFromDomain(order)
            val savedEntity = jpaOrderRepository.save(existingEntity)
            savedEntity.toDomain()
        } else {
            // Create new entity
            val orderEntity = OrderEntity.fromDomain(order)
            val savedEntity = jpaOrderRepository.save(orderEntity)
            savedEntity.toDomain()
        }
    }
    
    @Transactional(readOnly = true)
    override fun findById(id: OrderId): Order? {
        return jpaOrderRepository.findByIdWithLineItems(id.value)?.toDomain()
    }
    
    override fun delete(order: Order) {
        order.id?.let { id ->
            jpaOrderRepository.deleteById(id.value)
        }
    }
}