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
     * Perform an upsert operation for the given list of entities.
     *
     * @param entities The list of entities to upsert
     * @param tableName The table name
     * @param <T> The entity type
     * @return The total number of rows affected
     */
    fun <T : Any> upsertAll(entities: List<T>, tableName: String): Int
}
