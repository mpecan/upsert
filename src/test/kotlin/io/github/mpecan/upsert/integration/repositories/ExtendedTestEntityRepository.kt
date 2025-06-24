package io.github.mpecan.upsert.integration.repositories

import io.github.mpecan.upsert.entity.ExtendedTestEntity
import io.github.mpecan.upsert.repository.UpsertRepository
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository for ExtendedTestEntity to test @MappedSuperclass support.
 */
interface ExtendedTestEntityRepository : UpsertRepository<ExtendedTestEntity, Long>, JpaRepository<ExtendedTestEntity, Long>