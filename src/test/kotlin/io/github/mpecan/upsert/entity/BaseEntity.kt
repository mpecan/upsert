package io.github.mpecan.upsert.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime

/**
 * Base entity class with common fields that can be inherited by other entities.
 * This tests @MappedSuperclass support in the upsert library.
 */
@MappedSuperclass
abstract class BaseEntity(
    @Column(name = "created_at")
    open val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    open val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "version")
    open val version: Int = 1
) {
    constructor() : this(LocalDateTime.now(), LocalDateTime.now(), 1)
}