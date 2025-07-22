package com.example.domain.model.order

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow

class BundleComponentItemKotestTest : StringSpec({
    // Test constants
    val validComponentProductId = ProductId.of(1L)
    val validComponentName = "Test Component"
    val validQuantity = 2
    
    "should create BundleComponentItem with valid inputs" {
        // When
        val component = BundleComponentItem.create(
            componentProductId = validComponentProductId,
            componentName = validComponentName,
            quantity = validQuantity
        )
        
        // Then
        component.componentProductId shouldBe validComponentProductId
        component.componentName shouldBe validComponentName
        component.getQuantity() shouldBe validQuantity
    }
    
    "should throw exception when creating BundleComponentItem with blank component name" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            BundleComponentItem.create(
                componentProductId = validComponentProductId,
                componentName = "",
                quantity = validQuantity
            )
        }
    }
    
    "should throw exception when creating BundleComponentItem with whitespace-only component name" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            BundleComponentItem.create(
                componentProductId = validComponentProductId,
                componentName = "   ",
                quantity = validQuantity
            )
        }
    }
    
    "should throw exception when creating BundleComponentItem with zero quantity" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            BundleComponentItem.create(
                componentProductId = validComponentProductId,
                componentName = validComponentName,
                quantity = 0
            )
        }
    }
    
    "should throw exception when creating BundleComponentItem with negative quantity" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            BundleComponentItem.create(
                componentProductId = validComponentProductId,
                componentName = validComponentName,
                quantity = -1
            )
        }
    }
    
    "should reconstitute BundleComponentItem from persistence" {
        // When
        val component = BundleComponentItem.reconstitute(
            componentProductId = validComponentProductId,
            componentName = validComponentName,
            quantity = validQuantity
        )
        
        // Then
        component.componentProductId shouldBe validComponentProductId
        component.componentName shouldBe validComponentName
        component.getQuantity() shouldBe validQuantity
    }
    
    "should update quantity" {
        // Given
        val component = BundleComponentItem.create(
            componentProductId = validComponentProductId,
            componentName = validComponentName,
            quantity = validQuantity
        )
        
        // When
        val updatedComponent = component.updateQuantity(5)
        
        // Then
        updatedComponent.getQuantity() shouldBe 5
        updatedComponent.componentProductId shouldBe validComponentProductId
        updatedComponent.componentName shouldBe validComponentName
    }
    
    "should throw exception when updating quantity to zero or negative" {
        // Given
        val component = BundleComponentItem.create(
            componentProductId = validComponentProductId,
            componentName = validComponentName,
            quantity = validQuantity
        )
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            component.updateQuantity(0)
        }
        
        shouldThrow<IllegalArgumentException> {
            component.updateQuantity(-1)
        }
    }
    
    "should check if same product as another component" {
        // Given
        val component1 = BundleComponentItem.create(
            componentProductId = ProductId.of(1L),
            componentName = "Component 1",
            quantity = 2
        )
        
        val component2 = BundleComponentItem.create(
            componentProductId = ProductId.of(1L),
            componentName = "Different Name",
            quantity = 3
        )
        
        val component3 = BundleComponentItem.create(
            componentProductId = ProductId.of(2L),
            componentName = "Component 3",
            quantity = 1
        )
        
        // Then
        component1.isSameProduct(component2) shouldBe true
        component1.isSameProduct(component3) shouldBe false
        component2.isSameProduct(component3) shouldBe false
    }
    
    "should check if represents specific product" {
        // Given
        val component = BundleComponentItem.create(
            componentProductId = ProductId.of(1L),
            componentName = validComponentName,
            quantity = validQuantity
        )
        
        // Then
        component.isProduct(ProductId.of(1L)) shouldBe true
        component.isProduct(ProductId.of(2L)) shouldBe false
    }
    
    "should implement equals and hashCode based on componentProductId" {
        // Given
        val component1 = BundleComponentItem.create(
            componentProductId = ProductId.of(1L),
            componentName = "Component 1",
            quantity = 2
        )
        
        val component2 = BundleComponentItem.create(
            componentProductId = ProductId.of(1L),
            componentName = "Different Name",
            quantity = 5
        )
        
        val component3 = BundleComponentItem.create(
            componentProductId = ProductId.of(2L),
            componentName = "Component 3",
            quantity = 2
        )
        
        // Then
        (component1 == component2) shouldBe true
        (component1 == component3) shouldBe false
        component1.hashCode() shouldBe component2.hashCode()
    }
    
    "should have meaningful toString representation" {
        // Given
        val component = BundleComponentItem.create(
            componentProductId = ProductId.of(1L),
            componentName = "Test Component",
            quantity = 3
        )
        
        // When
        val stringRepresentation = component.toString()
        
        // Then
        stringRepresentation shouldBe "BundleComponentItem(componentProductId=1, componentName='Test Component', quantity=3)"
    }
    
    "should maintain immutability when updating" {
        // Given
        val originalComponent = BundleComponentItem.create(
            componentProductId = validComponentProductId,
            componentName = validComponentName,
            quantity = validQuantity
        )
        
        // When
        val updatedComponent = originalComponent.updateQuantity(10)
        
        // Then
        originalComponent.getQuantity() shouldBe validQuantity // Original unchanged
        updatedComponent.getQuantity() shouldBe 10 // New instance with updated quantity
        (originalComponent === updatedComponent) shouldBe false // Different instances
    }
})