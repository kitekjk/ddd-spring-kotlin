package com.example.domain.model.common

import java.time.Instant

/**
 * Value object representing audit information for entities.
 * Contains creation and modification timestamps and user information.
 */
data class AuditInfo private constructor(
    val createdAt: Instant,
    val createdBy: String,
    val updatedAt: Instant,
    val updatedBy: String
) {
    companion object {
        /**
         * Creates a new AuditInfo instance for a newly created entity.
         */
        fun newInstance(createdBy: String): AuditInfo {
            val now = Instant.now()
            return AuditInfo(now, createdBy, now, createdBy)
        }
        
        /**
         * Creates an AuditInfo instance with the given values.
         */
        fun of(
            createdAt: Instant,
            createdBy: String,
            updatedAt: Instant,
            updatedBy: String
        ): AuditInfo {
            return AuditInfo(createdAt, createdBy, updatedAt, updatedBy)
        }
    }
    
    /**
     * Updates the audit information when an entity is modified.
     */
    fun update(updatedBy: String): AuditInfo {
        return copy(updatedAt = Instant.now(), updatedBy = updatedBy)
    }
}