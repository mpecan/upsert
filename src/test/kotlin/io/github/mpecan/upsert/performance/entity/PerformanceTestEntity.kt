package io.github.mpecan.upsert.performance.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * Entity class for performance testing.
 * This class has more fields than the basic JpaTestEntity to make it more realistic for performance testing.
 */
@Entity
@Table(name = "performance_test_entity")
data class PerformanceTestEntity(
    @Id
    val id: Long,

    val name: String,

    val description: String? = null,

    val active: Boolean = true,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime = LocalDateTime.now(),

    val counter: Int = 0,

    val amount: Double = 0.0,

    val code: String? = null,

    val tags: String? = null
) {
    constructor() : this(0, "", "", true, LocalDateTime.now(), LocalDateTime.now(), 0, 0.0, "", "")
}