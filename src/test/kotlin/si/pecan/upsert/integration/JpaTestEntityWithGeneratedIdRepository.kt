package si.pecan.upsert.integration

import si.pecan.upsert.entity.JpaTestEntityWithGeneratedId
import si.pecan.upsert.repository.UpsertRepository

/**
 * Test repository for JpaTestEntityWithGeneratedId.
 * This repository is used to test entities with @GeneratedValue annotations.
 */
interface JpaTestEntityWithGeneratedIdRepository : UpsertRepository<JpaTestEntityWithGeneratedId, Long>