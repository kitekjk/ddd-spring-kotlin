package com.example.domain.common

import java.time.Instant
import java.util.*

/**
 * Domain context interface that provides common information for domain operations.
 * Contains request metadata and user information.
 */
interface DomainContext {
    val serviceName: String
    val userId: String
    val userName: String
    val roleId: String
    val requestId: UUID
    val requestedAt: Instant
    val clientIp: String
}

/**
 * Default implementation of DomainContext.
 */
data class DefaultDomainContext(
    override val serviceName: String,
    override val userId: String,
    override val userName: String,
    override val roleId: String,
    override val requestId: UUID,
    override val requestedAt: Instant,
    override val clientIp: String
) : DomainContext {
    
    companion object {
        /**
         * Creates a default domain context for system operations.
         */
        fun system(): DomainContext {
            return DefaultDomainContext(
                serviceName = "order-service",
                userId = "system",
                userName = "System",
                roleId = "SYSTEM",
                requestId = UUID.randomUUID(),
                requestedAt = Instant.now(),
                clientIp = "127.0.0.1"
            )
        }
        
        /**
         * Creates a domain context for user operations.
         */
        fun user(
            serviceName: String = "order-service",
            userId: String,
            userName: String,
            roleId: String,
            requestId: UUID = UUID.randomUUID(),
            requestedAt: Instant = Instant.now(),
            clientIp: String = "unknown"
        ): DomainContext {
            return DefaultDomainContext(
                serviceName = serviceName,
                userId = userId,
                userName = userName,
                roleId = roleId,
                requestId = requestId,
                requestedAt = requestedAt,
                clientIp = clientIp
            )
        }
    }
}