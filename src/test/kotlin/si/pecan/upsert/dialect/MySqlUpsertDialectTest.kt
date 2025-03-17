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
        val keyColumns = listOf(ColumnInfo("id", "id"))
        val valueColumns = listOf(ColumnInfo("name", "name"), ColumnInfo("description", "description"), ColumnInfo("active", "active"))

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, 1)

        // Then
        val expectedSql = "INSERT INTO test_table (id, name, description, active) VALUES (:id_1, :name_1, :description_1, :active_1) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), active = VALUES(active)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct upsert SQL for MySQL with multiple key columns`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf(ColumnInfo("id", "id"),ColumnInfo("code", "code"))
        val valueColumns = listOf(ColumnInfo("name", "name"), ColumnInfo("description", "description"), ColumnInfo("active", "active"))

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, 1)

        // Then
        val expectedSql = "INSERT INTO test_table (id, code, name, description, active) VALUES (:id_1, :code_1, :name_1, :description_1, :active_1) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), active = VALUES(active)"
        assertEquals(expectedSql, sql)
    }
}
