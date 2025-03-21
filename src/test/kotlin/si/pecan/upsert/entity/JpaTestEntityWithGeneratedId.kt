package si.pecan.upsert.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * Test entity using JPA annotations for upsert operations with a generated ID.
 * The ID field is nullable and mutable (var) to support auto-generation.
 */
@Entity
@Table(name = "jpa_test_entity_with_generated_id", uniqueConstraints = [
    UniqueConstraint(name = "unique_name", columnNames = ["name"])
])
data class JpaTestEntityWithGeneratedId(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = true)
    var id: Long? = null,

    @Column(nullable = false)
    val name: String,
    
    val description: String? = null,
    
    val active: Boolean = true
) {
    constructor() : this(null, "", "", true)
}