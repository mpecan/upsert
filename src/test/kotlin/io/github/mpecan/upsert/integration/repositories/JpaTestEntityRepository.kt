package io.github.mpecan.upsert.integration.repositories

import io.github.mpecan.upsert.entity.JpaTestEntity
import io.github.mpecan.upsert.repository.UpsertRepository
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Test repository for JpaTestEntity.
 */
interface JpaTestEntityRepository : UpsertRepository<JpaTestEntity, Long>,
    JpaRepository<JpaTestEntity, Long>