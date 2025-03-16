package si.pecan.upsert.repository

/**
 * Interface for upsert operations.
 */
interface UpsertOperations {
    /**
     * Initialize the operations with entity class and ID class.
     * This method should be called once at startup to prepare the operations.
     *
     * @param entityClass The entity class
     * @param idClass The ID class
     * @param tableName The table name
     */
    fun initialize(entityClass: Class<*>, idClass: Class<*>, tableName: String)

    /**
     * Perform an upsert operation for the given entity.
     *
     * @param entity The entity to upsert
     * @param tableName The table name
     * @param <T> The entity type
     * @return The number of rows affected
     */
    fun <T : Any> upsert(entity: T, tableName: String): Int

    /**
     * Perform an upsert operation for the given entity with custom ON clause and ignored fields.
     *
     * @param entity The entity to upsert
     * @param tableName The table name
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @param <T> The entity type
     * @return The number of rows affected
     */
    fun <T : Any> upsert(
        entity: T, 
        tableName: String, 
        onFields: List<String>, 
        ignoredFields: List<String>, 
        ignoreAllFields: Boolean
    ): Int

    /**
     * Perform an upsert operation for the given list of entities.
     *
     * @param entities The list of entities to upsert
     * @param tableName The table name
     * @param <T> The entity type
     * @return The total number of rows affected
     */
    fun <T : Any> upsertAll(entities: List<T>, tableName: String): Int

    /**
     * Perform an upsert operation for the given list of entities with custom ON clause and ignored fields.
     *
     * @param entities The list of entities to upsert
     * @param tableName The table name
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @param <T> The entity type
     * @return The total number of rows affected
     */
    fun <T : Any> upsertAll(
        entities: List<T>, 
        tableName: String, 
        onFields: List<String>, 
        ignoredFields: List<String>, 
        ignoreAllFields: Boolean
    ): Int
}
