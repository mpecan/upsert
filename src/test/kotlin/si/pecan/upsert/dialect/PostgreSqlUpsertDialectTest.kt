package si.pecan.upsert.dialect

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for PostgreSqlUpsertDialect.
 */
class PostgreSqlUpsertDialectTest {

    private val dialect = PostgreSqlUpsertDialect()

    @Test
    fun `should generate correct upsert SQL for PostgreSQL`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf("id")
        val valueColumns = listOf("name", "description", "active")

        // When
        val sql = dialect.generateUpsertSql(tableName, keyColumns, valueColumns)

        // Then
        val expectedSql = "INSERT INTO test_table (id, name, description, active) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, active = EXCLUDED.active"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct upsert SQL for PostgreSQL with multiple key columns`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf("id", "code")
        val valueColumns = listOf("name", "description", "active")

        // When
        val sql = dialect.generateUpsertSql(tableName, keyColumns, valueColumns)

        // Then
        val expectedSql = "INSERT INTO test_table (id, code, name, description, active) VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (id, code) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, active = EXCLUDED.active"
        assertEquals(expectedSql, sql)
    }
}