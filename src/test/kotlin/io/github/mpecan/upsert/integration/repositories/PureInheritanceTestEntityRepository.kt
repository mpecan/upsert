package io.github.mpecan.upsert.integration.repositories

import io.github.mpecan.upsert.entity.PureInheritanceTestEntity
import io.github.mpecan.upsert.repository.UpsertRepository
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository for PureInheritanceTestEntity to test @MappedSuperclass support.
 */
interface PureInheritanceTestEntityRepository : UpsertRepository<PureInheritanceTestEntity, Long>, JpaRepository<PureInheritanceTestEntity, Long>