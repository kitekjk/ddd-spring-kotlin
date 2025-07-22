package com.example.infrastructure.persistence.common

import com.example.domain.model.common.AuditInfo
import jakarta.persistence.*
import java.time.Instant

/**
 * Embedded object for audit information in JPA entities.
 */
@Embeddable
data class AuditInfoEmbeddable(
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "created_by", nullable = false, length = 100)
    var createdBy: String,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "updated_by", nullable = false, length = 100)
    var updatedBy: String
) {
    /**
     * Converts this embedded object to domain AuditInfo.
     */
    fun toDomain(): AuditInfo {
        return AuditInfo.of(
            createdAt = createdAt,
            createdBy = createdBy,
            updatedAt = updatedAt,
            updatedBy = updatedBy
        )
    }

    companion object {
        /**
         * Creates an AuditInfoEmbeddable from domain AuditInfo.
         */
        fun fromDomain(auditInfo: AuditInfo): AuditInfoEmbeddable {
            return AuditInfoEmbeddable(
                createdAt = auditInfo.createdAt,
                createdBy = auditInfo.createdBy,
                updatedAt = auditInfo.updatedAt,
                updatedBy = auditInfo.updatedBy
            )
        }
    }

    /**
     * Updates this embedded object from domain AuditInfo.
     */
    fun updateFromDomain(auditInfo: AuditInfo) {
        this.createdAt = auditInfo.createdAt
        this.createdBy = auditInfo.createdBy
        this.updatedAt = auditInfo.updatedAt
        this.updatedBy = auditInfo.updatedBy
    }
}