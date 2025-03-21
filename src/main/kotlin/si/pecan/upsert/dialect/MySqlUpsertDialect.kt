package si.pecan.upsert.dialect

/**
 * MySQL implementation of the UpsertDialect interface.
 * Uses the "INSERT ... ON DUPLICATE KEY UPDATE" syntax for upsert operations.
 */
class MySqlUpsertDialect : UpsertDialect {

    /**
     * Generate a batch upsert SQL statement for MySQL.
     * Uses the "INSERT ... VALUES (...), (...), ... ON DUPLICATE KEY UPDATE" syntax.
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
        updateColumns: List<ColumnInfo>,
        batchSize: Int
    ): String {

        // Create placeholders for all entities in the batch
        val allPlaceholders = (1..batchSize).joinToString(", ") {
            "(${valueColumns.map { column -> ":${column.name}_${it}" }.joinToString(", ")})"
        }

        val insertClause =
            "INSERT INTO $tableName (${valueColumns.joinToString(", ") { it.name }}) VALUES $allPlaceholders"

        val updateClause = updateColumns.joinToString(", ") { "${it.name} = VALUES(${it.name})" }

        return "$insertClause ON DUPLICATE KEY UPDATE $updateClause"
    }
}
