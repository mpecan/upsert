package si.pecan.upsert.dialect

/**
 * MySQL implementation of the UpsertDialect interface.
 * Uses the "INSERT ... ON DUPLICATE KEY UPDATE" syntax for upsert operations.
 */
class MySqlUpsertDialect : UpsertDialect {
    /**
     * Generate an upsert SQL statement for MySQL.
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

        val updateClause = valueColumns.joinToString(", ") { "$it = VALUES($it)" }

        return "$insertClause ON DUPLICATE KEY UPDATE $updateClause"
    }

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
        keyColumns: List<String>,
        valueColumns: List<String>,
        batchSize: Int
    ): String {
        val allColumns = keyColumns + valueColumns
        val placeholdersPerEntity = allColumns.indices.map { "?" }.joinToString(", ")

        // Create placeholders for all entities in the batch
        val allPlaceholders = (1..batchSize).joinToString(", ") { "($placeholdersPerEntity)" }

        val insertClause = "INSERT INTO $tableName (${allColumns.joinToString(", ")}) VALUES $allPlaceholders"

        val updateClause = valueColumns.joinToString(", ") { "$it = VALUES($it)" }

        return "$insertClause ON DUPLICATE KEY UPDATE $updateClause"
    }
}
