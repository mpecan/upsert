package si.pecan.upsert.integration

import si.pecan.upsert.entity.JpaTestEntity
import si.pecan.upsert.repository.UpsertRepository

/**
 * Test repository for JpaTestEntity.
 */
interface JpaTestEntityRepository : UpsertRepository<JpaTestEntity, Long>