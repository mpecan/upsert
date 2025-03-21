package si.pecan.upsert.dialect

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for MySqlUpsertDialect.
 */
class MySqlUpsertDialectTest {

    private val dialect = MySqlUpsertDialect()

    @Test
    fun `should generate correct upsert SQL for MySQL`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf(ColumnInfo("id", "id", Int::class.java, 4, false))
        val valueColumns = listOf(
            ColumnInfo("name", "name", String::class.java, 12),
            ColumnInfo("description", "description", String::class.java, 12, false),
            ColumnInfo("active", "active", Boolean::class.java, 16, false)
        )

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, keyColumns + valueColumns, valueColumns, 1)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, name, description, active) VALUES (:id_1, :name_1, :description_1, :active_1) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), active = VALUES(active)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct upsert SQL for MySQL with multiple key columns`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf(
            ColumnInfo("id", "id", Int::class.java, 4, false),
            ColumnInfo("code", "code", String::class.java, 12)
        )
        val valueColumns = listOf(
            ColumnInfo("name", "name", String::class.java, 12),
            ColumnInfo("description", "description", String::class.java, 12),
            ColumnInfo("active", "active", Boolean::class.java, 16)
        )

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, keyColumns + valueColumns, valueColumns,1)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, code, name, description, active) VALUES (:id_1, :code_1, :name_1, :description_1, :active_1) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), active = VALUES(active)"
        assertEquals(expectedSql, sql)
    }
}
