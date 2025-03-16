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
        val keyColumns = listOf("id")
        val valueColumns = listOf("name", "description", "active")

        // When
        val sql = dialect.generateUpsertSql(tableName, keyColumns, valueColumns)

        // Then
        val expectedSql = "INSERT INTO test_table (id, name, description, active) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), active = VALUES(active)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct upsert SQL for MySQL with multiple key columns`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf("id", "code")
        val valueColumns = listOf("name", "description", "active")

        // When
        val sql = dialect.generateUpsertSql(tableName, keyColumns, valueColumns)

        // Then
        val expectedSql = "INSERT INTO test_table (id, code, name, description, active) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), active = VALUES(active)"
        assertEquals(expectedSql, sql)
    }
}