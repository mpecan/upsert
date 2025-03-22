package io.github.mpecan.upsert.performance

import io.github.mpecan.upsert.repository.UpsertRepository
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository interface for performance testing.
 * This interface extends both UpsertRepository (for upsert operations) and JpaRepository (for saveAll operations).
 */
interface PerformanceTestRepository : UpsertRepository<PerformanceTestEntity, Long>,
    JpaRepository<PerformanceTestEntity, Long>