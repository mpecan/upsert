package io.github.mpecan.upsert.entity

import jakarta.persistence.*

/**
 * Test entity that purely inherits from BaseEntity without overriding any fields.
 * This entity tests true @MappedSuperclass inheritance where fields are only defined
 * in the parent class and inherited by the child.
 */
@Entity
@Table(name = "pure_inheritance_test_entity")
data class PureInheritanceTestEntity(
    @Id
    @Column(name = "id")
    val id: Long,
    
    @Column(name = "name")
    val name: String,
    
    @Column(name = "description")
    val description: String? = null
    
    // No explicit fields from BaseEntity - they should be purely inherited
) : BaseEntity() {
    constructor() : this(0L, "", null)
}