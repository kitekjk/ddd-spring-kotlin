package com.example.domain.model.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Instant
import java.time.temporal.ChronoUnit

class AuditInfoKotestTest : StringSpec({
    "should create new instance with same created and updated timestamps" {
        // Given
        val creator = "test-user"
        val beforeTest = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        // When
        val auditInfo = AuditInfo.newInstance(creator)
        
        // Then
        auditInfo.createdBy shouldBe creator
        auditInfo.updatedBy shouldBe creator
        auditInfo.createdAt shouldBe auditInfo.updatedAt
        auditInfo.createdAt.isAfter(beforeTest) shouldBe true
    }
    
    "should create instance with specified values" {
        // Given
        val createdAt = Instant.now().minus(2, ChronoUnit.DAYS)
        val createdBy = "creator-user"
        val updatedAt = Instant.now().minus(1, ChronoUnit.DAYS)
        val updatedBy = "updater-user"
        
        // When
        val auditInfo = AuditInfo.of(createdAt, createdBy, updatedAt, updatedBy)
        
        // Then
        auditInfo.createdAt shouldBe createdAt
        auditInfo.createdBy shouldBe createdBy
        auditInfo.updatedAt shouldBe updatedAt
        auditInfo.updatedBy shouldBe updatedBy
    }
    
    "should update with new updatedBy and current timestamp" {
        // Given
        val createdAt = Instant.now().minus(2, ChronoUnit.DAYS)
        val createdBy = "creator-user"
        val updatedAt = Instant.now().minus(1, ChronoUnit.DAYS)
        val updatedBy = "updater-user"
        val newUpdater = "new-updater"
        val auditInfo = AuditInfo.of(createdAt, createdBy, updatedAt, updatedBy)
        val beforeUpdate = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        // When
        val updatedAuditInfo = auditInfo.update(newUpdater)
        
        // Then
        updatedAuditInfo.createdAt shouldBe createdAt
        updatedAuditInfo.createdBy shouldBe createdBy
        updatedAuditInfo.updatedBy shouldBe newUpdater
        updatedAuditInfo.updatedAt shouldNotBe updatedAt
        updatedAuditInfo.updatedAt.isAfter(beforeUpdate) shouldBe true
    }
    
    "should be immutable and not change original instance on update" {
        // Given
        val auditInfo = AuditInfo.newInstance("original-user")
        val newUpdater = "new-updater"
        
        // When
        val updatedAuditInfo = auditInfo.update(newUpdater)
        
        // Then
        updatedAuditInfo shouldNotBe auditInfo
        auditInfo.updatedBy shouldBe "original-user"
        updatedAuditInfo.updatedBy shouldBe newUpdater
    }
})