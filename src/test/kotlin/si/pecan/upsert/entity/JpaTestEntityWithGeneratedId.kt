package si.pecan.upsert.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

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