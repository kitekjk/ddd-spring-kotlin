package com.example.infrastructure.persistence.order

import com.example.domain.common.DefaultDomainContext
import com.example.domain.model.order.*
import com.example.domain.model.user.UserId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.TestConstructor
import java.math.BigDecimal

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OrderRepositoryImplKotestTest(
    private val entityManager: TestEntityManager,
    private val jpaOrderRepository: JpaOrderRepository
) : BehaviorSpec({
    
    val orderRepository = OrderRepositoryImpl(jpaOrderRepository)
    
    given("새로운 Order 도메인 객체") {
        val customerId = UserId.of(1L)
        val lineItem = OrderLineItem.create(
            productId = ProductId.of(1L),
            productName = "Test Product",
            quantity = 2,
            unitPrice = BigDecimal("50.00")
        )
        
        val bundleComponent = BundleComponentItem.create(
            componentProductId = ProductId.of(2L),
            componentName = "Bundle Component",
            quantity = 1
        )
        
        val lineItemWithBundle = lineItem.addBundleComponent(bundleComponent)
        
        val testUserContext = DefaultDomainContext.user(userId = "testUser", userName = "Test User", roleId = "USER")
        val newOrder = Order.create(
            context = testUserContext,
            customerId = customerId,
            lineItems = listOf(lineItemWithBundle)
        )
        
        `when`("새 주문을 저장") {
            val savedOrder = orderRepository.save(newOrder)
            entityManager.flush()
            entityManager.clear()
            
            then("ID가 할당되어야 함") {
                savedOrder.id shouldNotBe null
                savedOrder.id!!.value shouldNotBe null
            }
            
            then("기본 속성들이 올바르게 저장되어야 함") {
                savedOrder.customerId shouldBe customerId
                savedOrder.getStatus() shouldBe OrderStatus.PENDING
                savedOrder.totalAmount shouldBe BigDecimal("100.00")
                savedOrder.auditInfo.createdBy shouldBe "testUser"
            }
            
            then("라인 아이템이 올바르게 저장되어야 함") {
                savedOrder.getLineItems().size shouldBe 1
                val savedLineItem = savedOrder.getLineItems().first()
                savedLineItem.productId.value shouldBe 1L
                savedLineItem.productName shouldBe "Test Product"
                savedLineItem.getQuantity() shouldBe 2
                savedLineItem.unitPrice shouldBe BigDecimal("50.00")
            }
            
            then("번들 컴포넌트가 올바르게 저장되어야 함") {
                val savedLineItem = savedOrder.getLineItems().first()
                savedLineItem.getBundleComponents().size shouldBe 1
                val savedBundleComponent = savedLineItem.getBundleComponents().first()
                savedBundleComponent.componentProductId.value shouldBe 2L
                savedBundleComponent.componentName shouldBe "Bundle Component"
                savedBundleComponent.getQuantity() shouldBe 1
            }
        }
    }
    
    given("저장된 Order") {
        val customerId = UserId.of(2L)
        val lineItem = OrderLineItem.create(
            productId = ProductId.of(3L),
            productName = "Existing Product",
            quantity = 1,
            unitPrice = BigDecimal("75.00")
        )
        
        val existingUserContext = DefaultDomainContext.user(userId = "existingUser", userName = "Existing User", roleId = "USER")
        val existingOrder = Order.create(
            context = existingUserContext,
            customerId = customerId,
            lineItems = listOf(lineItem)
        )
        
        val savedOrder = orderRepository.save(existingOrder)
        entityManager.flush()
        entityManager.clear()
        
        `when`("ID로 주문을 조회") {
            val foundOrder = orderRepository.findById(savedOrder.id!!)
            
            then("주문이 조회되어야 함") {
                foundOrder shouldNotBe null
                foundOrder!!.id shouldBe savedOrder.id
                foundOrder.customerId shouldBe customerId
                foundOrder.getStatus() shouldBe OrderStatus.PENDING
            }
            
            then("라인 아이템이 함께 조회되어야 함") {
                foundOrder!!.getLineItems().size shouldBe 1
                val foundLineItem = foundOrder.getLineItems().first()
                foundLineItem.productId.value shouldBe 3L
                foundLineItem.productName shouldBe "Existing Product"
            }
        }
        
        `when`("주문 상태를 업데이트") {
            val updatedUserContext = DefaultDomainContext.user(userId = "updatedUser", userName = "Updated User", roleId = "USER")
            val updatedOrder = savedOrder.pay(updatedUserContext)
            val savedUpdatedOrder = orderRepository.save(updatedOrder)
            entityManager.flush()
            entityManager.clear()
            
            then("상태가 업데이트되어야 함") {
                savedUpdatedOrder.getStatus() shouldBe OrderStatus.PAID
                savedUpdatedOrder.auditInfo.updatedBy shouldBe "updatedUser"
                savedUpdatedOrder.auditInfo.updatedAt shouldNotBe savedOrder.auditInfo.updatedAt
            }
            
            then("데이터베이스에서 조회해도 업데이트된 상태여야 함") {
                val reloadedOrder = orderRepository.findById(savedOrder.id!!)
                reloadedOrder!!.getStatus() shouldBe OrderStatus.PAID
                reloadedOrder.auditInfo.updatedBy shouldBe "updatedUser"
            }
        }
        
        `when`("주문을 삭제") {
            orderRepository.delete(savedOrder)
            entityManager.flush()
            entityManager.clear()
            
            then("주문이 삭제되어야 함") {
                val deletedOrder = orderRepository.findById(savedOrder.id!!)
                deletedOrder shouldBe null
            }
        }
    }
    
    given("존재하지 않는 주문 ID") {
        val nonExistentId = OrderId.of(99999L)
        
        `when`("ID로 주문을 조회") {
            val foundOrder = orderRepository.findById(nonExistentId)
            
            then("null이 반환되어야 함") {
                foundOrder shouldBe null
            }
        }
    }
})