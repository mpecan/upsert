package si.pecan.upsert.dialect

/**
 * Interface for database-specific upsert SQL generation.
 * Different databases have different syntax for upsert operations,
 * so this interface allows for database-specific implementations.
 */
interface UpsertDialect {
    /**
     * Generate an upsert SQL statement for the given entity.
     *
     * @param tableName The name of the table
     * @param keyColumns The columns to use as keys for the upsert operation
     * @param valueColumns The columns to update during the upsert operation
     * @return The generated SQL statement
     */
    fun generateUpsertSql(
        tableName: String,
        keyColumns: List<String>,
        valueColumns: List<String>
    ): String

    /**
     * Generate a batch upsert SQL statement for multiple entities.
     *
     * @param tableName The name of the table
     * @param keyColumns The columns to use as keys for the upsert operation
     * @param valueColumns The columns to update during the upsert operation
     * @param batchSize The number of entities in the batch
     * @return The generated SQL statement
     */
    fun generateBatchUpsertSql(
        tableName: String,
        keyColumns: List<String>,
        valueColumns: List<String>,
        batchSize: Int
    ): String

    /**
     * Check if this dialect supports optimized batch operations.
     * 
     * @return True if optimized batch operations are supported, false otherwise
     */
    fun supportsOptimizedBatch(): Boolean = false

    /**
     * Generate an optimized batch upsert SQL statement for multiple entities.
     * This is used by dialects that support more efficient batch operations,
     * such as PostgreSQL's unnest function.
     *
     * The default implementation falls back to the regular batch method.
     *
     * @param tableName The name of the table
     * @param keyColumns The columns to use as keys for the upsert operation
     * @param valueColumns The columns to update during the upsert operation
     * @param batchSize The number of entities in the batch
     * @return The generated SQL statement
     */
    fun generateOptimizedBatchUpsertSql(
        tableName: String,
        keyColumns: List<String>,
        valueColumns: List<String>,
        batchSize: Int
    ): String = generateBatchUpsertSql(tableName, keyColumns, valueColumns, batchSize)
}
