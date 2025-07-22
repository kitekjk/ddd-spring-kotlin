package com.example.infrastructure.persistence.order

import com.example.domain.model.common.AuditInfo
import com.example.domain.common.DefaultDomainContext
import com.example.domain.model.order.*
import com.example.domain.model.user.UserId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal
import java.time.Instant

class OrderEntityKotestTest : BehaviorSpec({
    
    given("OrderEntity와 Order 도메인 객체") {
        val customerId = UserId.of(1L)
        val orderDate = Instant.now()
        val totalAmount = BigDecimal("100.00")
        val auditInfo = AuditInfo.of(
            createdAt = Instant.now(),
            createdBy = "testUser",
            updatedAt = Instant.now(),
            updatedBy = "testUser"
        )
        
        val lineItem = OrderLineItem.create(
            productId = ProductId.of(1L),
            productName = "Test Product",
            quantity = 2,
            unitPrice = BigDecimal("50.00")
        )
        
        val order = Order.reconstitute(
            id = OrderId.of(1L),
            customerId = customerId,
            orderDate = orderDate,
            status = OrderStatus.PENDING,
            totalAmount = totalAmount,
            auditInfo = auditInfo,
            lineItems = listOf(lineItem)
        )
        
        `when`("도메인 객체에서 엔티티로 변환") {
            val entity = OrderEntity.fromDomain(order)
            
            then("기본 속성들이 올바르게 매핑되어야 함") {
                entity.id shouldBe 1L
                entity.customerId shouldBe 1L
                entity.orderDate shouldBe orderDate
                entity.status shouldBe "PENDING"
                entity.totalAmount shouldBe totalAmount
                entity.auditInfo.createdAt shouldBe auditInfo.createdAt
                entity.auditInfo.createdBy shouldBe auditInfo.createdBy
                entity.auditInfo.updatedAt shouldBe auditInfo.updatedAt
                entity.auditInfo.updatedBy shouldBe auditInfo.updatedBy
            }
            
            then("라인 아이템이 올바르게 매핑되어야 함") {
                entity.lineItems.size shouldBe 1
                val lineItemEntity = entity.lineItems.first()
                lineItemEntity.productId shouldBe 1L
                lineItemEntity.productName shouldBe "Test Product"
                lineItemEntity.quantity shouldBe 2
                lineItemEntity.unitPrice shouldBe BigDecimal("50.00")
                lineItemEntity.order shouldBe entity
            }
        }
        
        `when`("엔티티에서 도메인 객체로 변환") {
            val entity = OrderEntity.fromDomain(order)
            val convertedOrder = entity.toDomain()
            
            then("기본 속성들이 올바르게 변환되어야 함") {
                convertedOrder.id?.value shouldBe 1L
                convertedOrder.customerId.value shouldBe 1L
                convertedOrder.orderDate shouldBe orderDate
                convertedOrder.getStatus() shouldBe OrderStatus.PENDING
                convertedOrder.totalAmount shouldBe totalAmount
                convertedOrder.auditInfo.createdAt shouldBe auditInfo.createdAt
                convertedOrder.auditInfo.createdBy shouldBe auditInfo.createdBy
                convertedOrder.auditInfo.updatedAt shouldBe auditInfo.updatedAt
                convertedOrder.auditInfo.updatedBy shouldBe auditInfo.updatedBy
            }
            
            then("라인 아이템이 올바르게 변환되어야 함") {
                convertedOrder.getLineItems().size shouldBe 1
                val convertedLineItem = convertedOrder.getLineItems().first()
                convertedLineItem.productId.value shouldBe 1L
                convertedLineItem.productName shouldBe "Test Product"
                convertedLineItem.getQuantity() shouldBe 2
                convertedLineItem.unitPrice shouldBe BigDecimal("50.00")
            }
        }
        
        `when`("엔티티를 도메인 객체로 업데이트") {
            val entity = OrderEntity.fromDomain(order)
            val updaterContext = DefaultDomainContext.user(userId = "updatedUser", userName = "Updated User", roleId = "USER")
            val updatedOrder = order.pay(updaterContext)
            
            entity.updateFromDomain(updatedOrder)
            
            then("상태가 업데이트되어야 함") {
                entity.status shouldBe "PAID"
                entity.auditInfo.updatedBy shouldBe "updatedUser"
                entity.auditInfo.updatedAt shouldNotBe auditInfo.updatedAt
            }
        }
    }
    
    given("새로운 Order 도메인 객체 (ID 없음)") {
        val customerId = UserId.of(2L)
        val lineItem = OrderLineItem.create(
            productId = ProductId.of(2L),
            productName = "New Product",
            quantity = 1,
            unitPrice = BigDecimal("25.00")
        )
        
        val newUserContext = DefaultDomainContext.user(userId = "newUser", userName = "New User", roleId = "USER")
        val newOrder = Order.create(
            context = newUserContext,
            customerId = customerId,
            lineItems = listOf(lineItem)
        )
        
        `when`("도메인 객체에서 엔티티로 변환") {
            val entity = OrderEntity.fromDomain(newOrder)
            
            then("ID가 null이어야 함") {
                entity.id shouldBe null
            }
            
            then("기본 속성들이 올바르게 매핑되어야 함") {
                entity.customerId shouldBe 2L
                entity.status shouldBe "PENDING"
                entity.totalAmount shouldBe BigDecimal("25.00")
                entity.auditInfo.createdBy shouldBe "newUser"
            }
        }
    }
})