package si.pecan.upsert.integration

import si.pecan.upsert.entity.JpaTestEntity

/**
 * Test repository for JpaTestEntity with custom upsert methods.
 * This interface extends JpaTestEntityRepository and adds custom methods
 * to test the method name parsing functionality.
 */
interface CustomMethodsTestRepository : JpaTestEntityRepository {

    /**
     * Upsert a single entity using 'name' as the ON clause.
     */
    fun upsertOnId(entity: JpaTestEntity): Int

    /**
     * Upsert a single entity using 'name' as the ON clause.
     */
    fun upsertIgnoringName(entity: JpaTestEntity): Int

    /**
     * Upsert a single entity using 'name' as the ON clause.
     */
    fun upsertOnName(entity: JpaTestEntity): Int
    
    /**
     * Upsert a single entity using 'name' as the ON clause and ignoring 'active' field.
     */
    fun upsertOnNameIgnoringActive(entity: JpaTestEntity): Int
    
    /**
     * Upsert a single entity using 'name' and 'description' as the ON clause.
     */
    fun upsertOnNameAndDescription(entity: JpaTestEntity): Int
    
    /**
     * Upsert a single entity using 'name' as the ON clause and ignoring all fields.
     * This will only insert new rows and not update existing ones.
     */
    fun upsertOnNameIgnoringAllFields(entity: JpaTestEntity): Int
    
    /**
     * Upsert multiple entities using 'name' as the ON clause.
     */
    fun upsertAllOnName(entities: List<JpaTestEntity>): Int
    
    /**
     * Upsert multiple entities using 'name' as the ON clause and ignoring 'active' field.
     */
    fun upsertAllOnNameIgnoringActive(entities: List<JpaTestEntity>): Int
    
    /**
     * Upsert multiple entities using 'name' and 'description' as the ON clause.
     */
    fun upsertAllOnNameAndDescription(entities: List<JpaTestEntity>): Int
    
    /**
     * Upsert multiple entities using 'name' as the ON clause and ignoring all fields.
     * This will only insert new rows and not update existing ones.
     */
    fun upsertAllOnNameIgnoringAllFields(entities: List<JpaTestEntity>): Int
}