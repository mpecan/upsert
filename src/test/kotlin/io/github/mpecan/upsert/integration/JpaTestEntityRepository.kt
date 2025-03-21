package io.github.mpecan.upsert.integration

import io.github.mpecan.upsert.entity.JpaTestEntity
import io.github.mpecan.upsert.repository.UpsertRepository

/**
 * Test repository for JpaTestEntity.
 */
interface JpaTestEntityRepository : UpsertRepository<JpaTestEntity, Long>