package si.pecan.upsert.repository

import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository

/**
 * Base repository interface with upsert capabilities.
 * This interface extends Spring Data's Repository interface and adds upsert functionality.
 *
 * @param T The entity type
 * @param ID The type of the entity's ID
 */
@NoRepositoryBean
interface BaseUpsertRepository<T : Any, ID> : Repository<T, ID> {
    
    /**
     * Perform an upsert operation for the given entity.
     *
     * @param entity The entity to upsert
     * @return The number of rows affected
     */
    fun upsert(entity: T): Int
    
    /**
     * Perform an upsert operation for the given list of entities.
     *
     * @param entities The list of entities to upsert
     * @return The total number of rows affected
     */
    fun upsertAll(entities: List<T>): Int

    /**
     * Perform an upsert operation for the given entity with custom ON clause and ignored fields.
     *
     * @param entity The entity to upsert
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @return The number of rows affected
     */
    fun upsert(
        entity: T,
        onFields: List<String>,
        ignoredFields: List<String>,
        ignoreAllFields: Boolean
    ): Int

    /**
     * Perform an upsert operation for the given list of entities with custom ON clause and ignored fields.
     *
     * @param entities The list of entities to upsert
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @return The total number of rows affected
     */
    fun upsertAll(
        entities: List<T>,
        onFields: List<String>,
        ignoredFields: List<String>,
        ignoreAllFields: Boolean
    ): Int
}