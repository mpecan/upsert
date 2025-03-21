package io.github.mpecan.upsert.integration

import io.github.mpecan.upsert.entity.JpaTestEntityWithGeneratedId
import io.github.mpecan.upsert.repository.UpsertRepository

/**
 * Test repository for JpaTestEntityWithGeneratedId.
 * This repository is used to test entities with @GeneratedValue annotations.
 */
interface JpaTestEntityWithGeneratedIdRepository : UpsertRepository<JpaTestEntityWithGeneratedId, Long>