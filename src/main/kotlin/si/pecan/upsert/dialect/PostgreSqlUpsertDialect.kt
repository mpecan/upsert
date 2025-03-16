package si.pecan.upsert.dialect

/**
 * PostgreSQL implementation of the UpsertDialect interface.
 * Uses the "INSERT ... ON CONFLICT ... DO UPDATE" syntax for upsert operations.
 */
class PostgreSqlUpsertDialect : UpsertDialect {
    /**
     * Generate an upsert SQL statement for PostgreSQL.
     *
     * @param tableName The name of the table
     * @param keyColumns The columns to use as keys for the upsert operation
     * @param valueColumns The columns to update during the upsert operation
     * @return The generated SQL statement
     */
    override fun generateUpsertSql(
        tableName: String,
        keyColumns: List<String>,
        valueColumns: List<String>
    ): String {
        val allColumns = keyColumns + valueColumns
        val placeholders = allColumns.indices.map { "?" }.joinToString(", ")

        val insertClause = "INSERT INTO $tableName (${allColumns.joinToString(", ")}) VALUES ($placeholders)"

        val onConflictClause = "ON CONFLICT (${keyColumns.joinToString(", ")}) DO UPDATE SET"

        val updateClause = valueColumns.joinToString(", ") { "$it = EXCLUDED.$it" }

        return "$insertClause $onConflictClause $updateClause"
    }

    /**
     * Generate a batch upsert SQL statement for PostgreSQL.
     * Uses the "INSERT ... VALUES (...), (...), ... ON CONFLICT ... DO UPDATE" syntax.
     *
     * @param tableName The name of the table
     * @param keyColumns The columns to use as keys for the upsert operation
     * @param valueColumns The columns to update during the upsert operation
     * @param batchSize The number of entities in the batch
     * @return The generated SQL statement
     */
    override fun generateBatchUpsertSql(
        tableName: String,
        keyColumns: List<String>,
        valueColumns: List<String>,
        batchSize: Int
    ): String {
        val allColumns = keyColumns + valueColumns
        val placeholdersPerEntity = allColumns.indices.map { "?" }.joinToString(", ")

        // Create placeholders for all entities in the batch
        val allPlaceholders = (1..batchSize).joinToString(", ") { "($placeholdersPerEntity)" }

        val insertClause = "INSERT INTO $tableName (${allColumns.joinToString(", ")}) VALUES $allPlaceholders"

        val onConflictClause = "ON CONFLICT (${keyColumns.joinToString(", ")}) DO UPDATE SET"

        val updateClause = valueColumns.joinToString(", ") { "$it = EXCLUDED.$it" }

        return "$insertClause $onConflictClause $updateClause"
    }

    /**
     * Check if this dialect supports optimized batch operations.
     * PostgreSQL supports optimized batch operations using unnest.
     * 
     * @return True as PostgreSQL supports optimized batch operations
     */
    override fun supportsOptimizedBatch(): Boolean = true

    /**
     * Generate an optimized batch upsert SQL statement for PostgreSQL using unnest.
     * This is more efficient than the regular batch method for large batches.
     *
     * @param tableName The name of the table
     * @param keyColumns The columns to use as keys for the upsert operation
     * @param valueColumns The columns to update during the upsert operation
     * @param batchSize The number of entities in the batch
     * @return The generated SQL statement
     */
    override fun generateOptimizedBatchUpsertSql(
        tableName: String,
        keyColumns: List<String>,
        valueColumns: List<String>,
        batchSize: Int
    ): String {
        val allColumns = keyColumns + valueColumns

        // Create unnest expressions for each column with named parameters
        val unnestExpressions = allColumns.map { column ->
            "unnest(:${column}_array) as $column"
        }.joinToString(", ")

        // Create the WITH clause using unnest
        val withClause = "WITH source_data AS (SELECT $unnestExpressions)"

        // Create the INSERT clause
        val insertClause = "INSERT INTO $tableName (${allColumns.joinToString(", ")})"

        // Create the SELECT clause
        val selectClause = "SELECT ${allColumns.joinToString(", ")} FROM source_data"

        // Create the ON CONFLICT clause
        val onConflictClause = "ON CONFLICT (${keyColumns.joinToString(", ")}) DO UPDATE SET"

        // Create the UPDATE clause
        val updateClause = valueColumns.joinToString(", ") { "$it = EXCLUDED.$it" }

        return "$withClause $insertClause $selectClause $onConflictClause $updateClause"
    }

    /**
     * Get the PostgreSQL type for a column.
     * This is a placeholder implementation that assumes all columns are of type TEXT.
     * In a real implementation, this would need to be determined based on the column type.
     *
     * @param column The column name
     * @return The PostgreSQL type for the column
     */
    private fun getColumnType(column: String): String {
        // In a real implementation, this would need to be determined based on the column type
        // For now, we'll assume all columns are of type TEXT
        return "TEXT[]"
    }
}
