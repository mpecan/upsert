package si.pecan.upsert.repository

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
