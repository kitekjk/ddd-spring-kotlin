package com.example.domain.event

import com.example.domain.common.DomainContext
import java.util.*

/**
 * Base interface for all domain events.
 * Domain events represent something that happened in the domain that is of interest to other parts of the system.
 */
interface DomainEvent<T> {
    /**
     * Unique identifier for this event instance.
     */
    val eventId: UUID
    
    /**
     * Timestamp when the event occurred (in milliseconds since epoch).
     */
    val occurredOn: Long
    
    /**
     * Domain context containing request metadata and user information.
     */
    val context: DomainContext
    
    /**
     * The actual event payload containing domain-specific data.
     */
    val payload: T
}

/**
 * Abstract base class for domain events that provides common functionality.
 */
abstract class DomainEventBase<T> : DomainEvent<T> {
    override val eventId: UUID = UUID.randomUUID()
    override val occurredOn: Long = System.currentTimeMillis()
}