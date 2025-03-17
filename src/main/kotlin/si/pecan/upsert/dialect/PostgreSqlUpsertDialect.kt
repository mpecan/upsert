package si.pecan.upsert.dialect

/**
 * PostgreSQL implementation of the UpsertDialect interface.
 * Uses the "INSERT ... ON CONFLICT ... DO UPDATE" syntax for upsert operations.
 */
class PostgreSqlUpsertDialect : UpsertDialect {
    /**
     * Generate a batch upsert SQL statement for PostgreSQL.
     * Uses the "INSERT ... VALUES (...) ON CONFLICT ... DO UPDATE" syntax.
     * Does not include a RETURNING clause, as generated keys will be handled by GeneratedKeyHolder.
     *
     * @param tableName The name of the table
     * @param keyColumns The columns to use as keys for the upsert operation
     * @param valueColumns The columns to update during the upsert operation
     * @param batchSize The number of entities in the batch
     * @return The generated SQL statement
     */
    override fun generateBatchUpsertSql(
        tableName: String,
        keyColumns: List<ColumnInfo>,
        valueColumns: List<ColumnInfo>,
        batchSize: Int
    ): String {
        val usableKeyColumns = keyColumns.filter { keyColumn -> !keyColumn.generated }
        val allColumns = (usableKeyColumns + valueColumns).toSet()

        val insertClause = "INSERT INTO $tableName (${
            allColumns.map { it.name }.joinToString(", ")
        }) VALUES (${allColumns.map { column -> ":${column.fieldName}" }.joinToString(", ")})"

        val onConflictClause =
            "ON CONFLICT (${usableKeyColumns.map { it.name }.joinToString(", ")}) DO UPDATE SET"

        val updateClause = valueColumns.filter { value ->  usableKeyColumns.none { key -> value.name == key.name } }.map { it.name }.joinToString(", ") { "$it = EXCLUDED.$it" }

        return "$insertClause $onConflictClause $updateClause"
    }

    /**
     * Check if this dialect supports optimized batch operations.
     * PostgreSQL supports optimized batch operations using unnest.
     *
     * @return True as PostgreSQL supports optimized batch operations
     */
    override fun supportsOptimizedBatch(): Boolean = true
}
