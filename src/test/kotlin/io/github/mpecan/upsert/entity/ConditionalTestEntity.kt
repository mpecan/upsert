package io.github.mpecan.upsert.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

/**
 * Test entity for conditional upsert operations.
 * Includes various data types to test comparison operators.
 */
@Entity
@Table(
    name = "conditional_test_entity",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["name", "version"])
    ]
)
data class ConditionalTestEntity(
    @Id
    val id: Long,
    
    val name: String,
    
    val version: Int,
    
    val price: Double,
    
    val score: Float,
    
    val updatedAt: LocalDateTime,
    
    val description: String? = null,
    
    val active: Boolean = true
) {
    constructor() : this(0, "", 0, 0.0, 0.0f, LocalDateTime.now(), null, true)
}