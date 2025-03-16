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
     * @return The updated entity with any autogenerated fields
     */
    fun upsert(entity: T): T

    /**
     * Perform an upsert operation for the given list of entities.
     *
     * @param entities The list of entities to upsert
     * @return The list of updated entities with any autogenerated fields
     */
    fun upsertAll(entities: List<T>): List<T>

    /**
     * Perform an upsert operation for the given entity with custom ON clause and ignored fields.
     *
     * @param entity The entity to upsert
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @return The updated entity with any autogenerated fields
     */
    fun upsert(
        entity: T,
        onFields: List<String>,
        ignoredFields: List<String>,
        ignoreAllFields: Boolean
    ): T

    /**
     * Perform an upsert operation for the given list of entities with custom ON clause and ignored fields.
     *
     * @param entities The list of entities to upsert
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @return The list of updated entities with any autogenerated fields
     */
    fun upsertAll(
        entities: List<T>,
        onFields: List<String>,
        ignoredFields: List<String>,
        ignoreAllFields: Boolean
    ): List<T>
}
