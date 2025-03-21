package io.github.mpecan.upsert.integration

import io.github.mpecan.upsert.entity.JpaTestEntity

/**
 * Test repository for JpaTestEntity with custom upsert methods.
 * This interface extends JpaTestEntityRepository and adds custom methods
 * to test the method name parsing functionality.
 */
interface CustomMethodsTestRepository : JpaTestEntityRepository {

    /**
     * Upsert a single entity using 'name' as the ON clause.
     */
    fun upsertOnId(entity: JpaTestEntity): JpaTestEntity

    /**
     * Upsert a single entity using 'name' as the ON clause.
     */
    fun upsertIgnoringName(entity: JpaTestEntity): JpaTestEntity

    /**
     * Upsert a single entity using 'name' as the ON clause.
     */
    fun upsertOnName(entity: JpaTestEntity): JpaTestEntity

    /**
     * Upsert a single entity using 'name' as the ON clause and ignoring 'active' field.
     */
    fun upsertOnNameIgnoringActive(entity: JpaTestEntity): JpaTestEntity

    /**
     * Upsert a single entity using 'name' and 'description' as the ON clause.
     */
    fun upsertOnNameAndDescription(entity: JpaTestEntity): JpaTestEntity

    /**
     * Upsert a single entity using 'name' as the ON clause and ignoring all fields.
     * This will only insert new rows and not update existing ones.
     */
    fun upsertOnNameIgnoringAllFields(entity: JpaTestEntity): JpaTestEntity

    /**
     * Upsert multiple entities using 'name' as the ON clause.
     */
    fun upsertAllOnName(entities: List<JpaTestEntity>): List<JpaTestEntity>

    /**
     * Upsert multiple entities using 'name' as the ON clause and ignoring 'active' field.
     */
    fun upsertAllOnNameIgnoringActive(entities: List<JpaTestEntity>): List<JpaTestEntity>

    /**
     * Upsert a single entity using 'id' as the ON clause and ignoring all fields.
     * This will only insert new rows and not update existing ones.
     */
    fun upsertOnIdIgnoringAllFields(entity: JpaTestEntity): JpaTestEntity
}
