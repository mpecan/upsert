package io.github.mpecan.upsert.integration.repositories

import io.github.mpecan.upsert.entity.JpaTestEntityWithGeneratedId
import io.github.mpecan.upsert.repository.UpsertRepository
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Test repository for JpaTestEntityWithGeneratedId.
 * This repository is used to test entities with @GeneratedValue annotations.
 */
interface JpaTestEntityWithGeneratedIdRepository :
    UpsertRepository<JpaTestEntityWithGeneratedId, Long>,
    JpaRepository<JpaTestEntityWithGeneratedId, Long>