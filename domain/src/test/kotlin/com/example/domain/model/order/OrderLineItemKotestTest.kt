package com.example.domain.model.order

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import java.math.BigDecimal

class OrderLineItemKotestTest : StringSpec({
    // Test constants
    val validProductId = ProductId.of(1L)
    val validProductName = "Test Product"
    val validQuantity = 2
    val validUnitPrice = BigDecimal("50.00")
    
    "should create OrderLineItem with valid inputs" {
        // When
        val lineItem = OrderLineItem.create(
            productId = validProductId,
            productName = validProductName,
            quantity = validQuantity,
            unitPrice = validUnitPrice
        )
        
        // Then
        lineItem.productId shouldBe validProductId
        lineItem.productName shouldBe validProductName
        lineItem.getQuantity() shouldBe validQuantity
        lineItem.unitPrice shouldBe validUnitPrice
        lineItem.getTotalPrice() shouldBe BigDecimal("100.00") // 2 * 50.00
        lineItem.getBundleComponents().size shouldBe 0
        lineItem.hasBundleComponents() shouldBe false
    }
    
    "should throw exception when creating OrderLineItem with blank product name" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            OrderLineItem.create(
                productId = validProductId,
                productName = "",
                quantity = validQuantity,
                unitPrice = validUnitPrice
            )
        }
    }
    
    "should throw exception when creating OrderLineItem with zero quantity" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            OrderLineItem.create(
                productId = validProductId,
                productName = validProductName,
                quantity = 0,
                unitPrice = validUnitPrice
            )
        }
    }
    
    "should throw exception when creating OrderLineItem with negative quantity" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            OrderLineItem.create(
                productId = validProductId,
                productName = validProductName,
                quantity = -1,
                unitPrice = validUnitPrice
            )
        }
    }
    
    "should throw exception when creating OrderLineItem with negative unit price" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            OrderLineItem.create(
                productId = validProductId,
                productName = validProductName,
                quantity = validQuantity,
                unitPrice = BigDecimal("-10.00")
            )
        }
    }
    
    "should allow zero unit price" {
        // When
        val lineItem = OrderLineItem.create(
            productId = validProductId,
            productName = validProductName,
            quantity = validQuantity,
            unitPrice = BigDecimal.ZERO
        )
        
        // Then
        lineItem.unitPrice shouldBe BigDecimal.ZERO
        lineItem.getTotalPrice() shouldBe BigDecimal.ZERO
    }
    
    "should reconstitute OrderLineItem from persistence" {
        // Given
        val bundleComponent = BundleComponentItem.create(
            componentProductId = ProductId.of(2L),
            componentName = "Component 1",
            quantity = 1
        )
        val bundleComponents = listOf(bundleComponent)
        
        // When
        val lineItem = OrderLineItem.reconstitute(
            productId = validProductId,
            productName = validProductName,
            quantity = validQuantity,
            unitPrice = validUnitPrice,
            bundleComponents = bundleComponents
        )
        
        // Then
        lineItem.productId shouldBe validProductId
        lineItem.productName shouldBe validProductName
        lineItem.getQuantity() shouldBe validQuantity
        lineItem.unitPrice shouldBe validUnitPrice
        lineItem.getBundleComponents().size shouldBe 1
        lineItem.hasBundleComponents() shouldBe true
    }
    
    "should update quantity" {
        // Given
        val lineItem = OrderLineItem.create(
            productId = validProductId,
            productName = validProductName,
            quantity = validQuantity,
            unitPrice = validUnitPrice
        )
        
        // When
        val updatedLineItem = lineItem.updateQuantity(5)
        
        // Then
        updatedLineItem.getQuantity() shouldBe 5
        updatedLineItem.getTotalPrice() shouldBe BigDecimal("250.00") // 5 * 50.00
        updatedLineItem.productId shouldBe validProductId
        updatedLineItem.productName shouldBe validProductName
        updatedLineItem.unitPrice shouldBe validUnitPrice
    }
    
    "should throw exception when updating quantity to zero or negative" {
        // Given
        val lineItem = OrderLineItem.create(
            productId = validProductId,
            productName = validProductName,
            quantity = validQuantity,
            unitPrice = validUnitPrice
        )
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            lineItem.updateQuantity(0)
        }
        
        shouldThrow<IllegalArgumentException> {
            lineItem.updateQuantity(-1)
        }
    }
    
    "should add bundle component" {
        // Given
        val lineItem = OrderLineItem.create(
            productId = validProductId,
            productName = validProductName,
            quantity = validQuantity,
            unitPrice = validUnitPrice
        )
        val bundleComponent = BundleComponentItem.create(
            componentProductId = ProductId.of(2L),
            componentName = "Component 1",
            quantity = 1
        )
        
        // When
        val updatedLineItem = lineItem.addBundleComponent(bundleComponent)
        
        // Then
        updatedLineItem.getBundleComponents().size shouldBe 1
        updatedLineItem.hasBundleComponents() shouldBe true
        updatedLineItem.containsComponent(ProductId.of(2L)) shouldBe true
        updatedLineItem.getComponentQuantity(ProductId.of(2L)) shouldBe 1
    }
    
    "should remove bundle component" {
        // Given
        val bundleComponent = BundleComponentItem.create(
            componentProductId = ProductId.of(2L),
            componentName = "Component 1",
            quantity = 1
        )
        val lineItem = OrderLineItem.create(
            productId = validProductId,
            productName = validProductName,
            quantity = validQuantity,
            unitPrice = validUnitPrice
        ).addBundleComponent(bundleComponent)
        
        // When
        val updatedLineItem = lineItem.removeBundleComponent(ProductId.of(2L))
        
        // Then
        updatedLineItem.getBundleComponents().size shouldBe 0
        updatedLineItem.hasBundleComponents() shouldBe false
        updatedLineItem.containsComponent(ProductId.of(2L)) shouldBe false
        updatedLineItem.getComponentQuantity(ProductId.of(2L)) shouldBe 0
    }
    
    "should handle multiple bundle components with same product ID" {
        // Given
        val lineItem = OrderLineItem.create(
            productId = validProductId,
            productName = validProductName,
            quantity = validQuantity,
            unitPrice = validUnitPrice
        )
        val component1 = BundleComponentItem.create(
            componentProductId = ProductId.of(2L),
            componentName = "Component 1",
            quantity = 2
        )
        val component2 = BundleComponentItem.create(
            componentProductId = ProductId.of(2L),
            componentName = "Component 2",
            quantity = 3
        )
        
        // When
        val updatedLineItem = lineItem
            .addBundleComponent(component1)
            .addBundleComponent(component2)
        
        // Then
        updatedLineItem.getBundleComponents().size shouldBe 2
        updatedLineItem.containsComponent(ProductId.of(2L)) shouldBe true
        updatedLineItem.getComponentQuantity(ProductId.of(2L)) shouldBe 5 // 2 + 3
    }
    
    "should calculate total price correctly" {
        // Given
        val lineItem1 = OrderLineItem.create(
            productId = ProductId.of(1L),
            productName = "Product 1",
            quantity = 3,
            unitPrice = BigDecimal("25.50")
        )
        
        val lineItem2 = OrderLineItem.create(
            productId = ProductId.of(2L),
            productName = "Product 2",
            quantity = 2,
            unitPrice = BigDecimal("15.25")
        )
        
        // Then
        lineItem1.getTotalPrice() shouldBe BigDecimal("76.50") // 3 * 25.50
        lineItem2.getTotalPrice() shouldBe BigDecimal("30.50") // 2 * 15.25
    }
    
    "should implement equals and hashCode based on productId" {
        // Given
        val lineItem1 = OrderLineItem.create(
            productId = ProductId.of(1L),
            productName = "Product 1",
            quantity = 2,
            unitPrice = BigDecimal("50.00")
        )
        
        val lineItem2 = OrderLineItem.create(
            productId = ProductId.of(1L),
            productName = "Different Name",
            quantity = 5,
            unitPrice = BigDecimal("100.00")
        )
        
        val lineItem3 = OrderLineItem.create(
            productId = ProductId.of(2L),
            productName = "Product 2",
            quantity = 2,
            unitPrice = BigDecimal("50.00")
        )
        
        // Then
        (lineItem1 == lineItem2) shouldBe true
        (lineItem1 == lineItem3) shouldBe false
        lineItem1.hashCode() shouldBe lineItem2.hashCode()
    }
})