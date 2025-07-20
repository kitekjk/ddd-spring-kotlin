package com.example.domain.model.order

/**
 * Repository interface for Order aggregate.
 * Defines operations for persisting and retrieving Order entities.
 */
interface OrderRepository {
    /**
     * Saves an Order entity.
     * If the Order has no ID, a new Order is created.
     * If the Order has an ID, the existing Order is updated.
     *
     * @param order The Order to save
     * @return The saved Order with ID assigned if it was a new Order
     */
    fun save(order: Order): Order

    /**
     * Finds an Order by its ID.
     *
     * @param id The ID of the Order to find
     * @return The Order if found, null otherwise
     */
    fun findById(id: OrderId): Order?

    /**
     * Deletes an Order.
     *
     * @param order The Order to delete
     */
    fun delete(order: Order)
}