package io.github.mpecan.upsert.integration.repositories

import io.github.mpecan.upsert.entity.JpaTestEntityWithConverter
import io.github.mpecan.upsert.repository.UpsertRepository
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Test repository for JpaTestEntityWithConverter.
 * This repository is used to test entities with custom converters.
 */
interface JpaTestEntityWithConverterRepository : UpsertRepository<JpaTestEntityWithConverter, Long>,
    JpaRepository<JpaTestEntityWithConverter, Long>