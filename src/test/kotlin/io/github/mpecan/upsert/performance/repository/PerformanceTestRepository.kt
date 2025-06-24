package io.github.mpecan.upsert.performance.repository

import io.github.mpecan.upsert.performance.entity.PerformanceTestEntity
import io.github.mpecan.upsert.repository.UpsertRepository
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository interface for performance testing.
 * This interface extends both UpsertRepository (for upsert operations) and JpaRepository (for saveAll operations).
 */
interface PerformanceTestRepository : UpsertRepository<PerformanceTestEntity, Long>,
    JpaRepository<PerformanceTestEntity, Long> {
    
    // Conditional upsert methods for performance testing
    fun upsertOnIdWhenUpdatedAtMore(entity: PerformanceTestEntity): PerformanceTestEntity
    fun upsertAllOnIdWhenUpdatedAtMore(entities: List<PerformanceTestEntity>): List<PerformanceTestEntity>
    
    fun upsertOnIdWhenCounterMoreOrEqual(entity: PerformanceTestEntity): PerformanceTestEntity
    fun upsertAllOnIdWhenCounterMoreOrEqual(entities: List<PerformanceTestEntity>): List<PerformanceTestEntity>
}