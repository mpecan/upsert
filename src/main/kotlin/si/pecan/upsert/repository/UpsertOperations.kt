package si.pecan.upsert.repository

import si.pecan.upsert.model.UpsertModel

/**
 * Interface for upsert operations.
 */
interface UpsertOperations {

    /**
     * Perform an upsert operation for the given entity.
     *
     * @param entity The entity to upsert
     * @param tableName The table name
     * @param <T> The entity type
     * @return The updated entity with any autogenerated fields
     */
    fun <T : Any> upsert(entity: T, tableName: String): T

    /**
     * Perform an upsert operation for the given entity with custom ON clause and ignored fields.
     *
     * @param entity The entity to upsert
     * @param tableName The table name
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @param <T> The entity type
     * @return The updated entity with any autogenerated fields
     */
    fun <T : Any> upsert(
        entity: T,
        tableName: String,
        onFields: List<String>,
        ignoredFields: List<String>,
        ignoreAllFields: Boolean
    ): T

    /**
     * Perform an upsert operation for the given list of entities.
     *
     * @param entities The list of entities to upsert
     * @param tableName The table name
     * @param <T> The entity type
     * @return The list of updated entities with any autogenerated fields
     */
    fun <T : Any> upsertAll(entities: List<T>, tableName: String): List<T>

    /**
     * Perform an upsert operation for the given list of entities with custom ON clause and ignored fields.
     *
     * @param entities The list of entities to upsert
     * @param tableName The table name
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @param <T> The entity type
     * @return The list of updated entities with any autogenerated fields
     */
    fun <T : Any> upsertAll(
        entities: List<T>,
        tableName: String,
        onFields: List<String>,
        ignoredFields: List<String>,
        ignoreAllFields: Boolean
    ): List<T>
}
