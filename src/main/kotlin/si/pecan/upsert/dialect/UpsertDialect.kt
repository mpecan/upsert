package si.pecan.upsert.dialect

import jakarta.persistence.UniqueConstraint

/**
 * Interface for database-specific upsert SQL generation.
 * Different databases have different syntax for upsert operations,
 * so this interface allows for database-specific implementations.
 */
interface UpsertDialect {

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
        keyColumns: List<ColumnInfo>,
        valueColumns: List<ColumnInfo>,
        updateColumns: List<ColumnInfo> = valueColumns,
        batchSize: Int
    ): String

    /**
     * Check if this dialect supports optimized batch operations.
     * 
     * @return True if optimized batch operations are supported, false otherwise
     */
    fun supportsOptimizedBatch(): Boolean = false
}
