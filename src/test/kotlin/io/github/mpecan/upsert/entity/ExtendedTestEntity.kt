package io.github.mpecan.upsert.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Test entity that extends BaseEntity to test @MappedSuperclass support.
 * This entity should inherit fields from BaseEntity (createdAt, updatedAt, version)
 * and add its own fields (id, name, description).
 */
@Entity
@Table(name = "extended_test_entity")
data class ExtendedTestEntity(
    @Id
    @Column(name = "id")
    val id: Long,
    
    @Column(name = "name")
    val name: String,
    
    @Column(name = "description")
    val description: String? = null,

    // Inherited fields from BaseEntity
    @Column(name = "created_at")
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "version")
    override val version: Int = 1
) : BaseEntity(createdAt, updatedAt, version) {
    constructor() : this(0L, "", null, LocalDateTime.now(), LocalDateTime.now(), 1)
}