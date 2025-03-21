package io.github.mpecan.upsert.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * Test entity using JPA annotations for upsert operations.
 */
@Entity
@Table(name = "jpa_test_entity")
data class JpaTestEntity(
    @Id
    val id: Long,
    
    val name: String,
    
    val description: String? = null,
    
    val active: Boolean = true
)