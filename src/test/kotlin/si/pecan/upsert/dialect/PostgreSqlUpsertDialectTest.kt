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

        val keyColumns = listOf(ColumnInfo("id", "id"))
        val valueColumns = listOf(ColumnInfo("name", "name"), ColumnInfo("description", "description"), ColumnInfo("active", "active"))

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, 1)

        // Then
        val expectedSql = "INSERT INTO test_table (id, name, description, active) VALUES (:id, :name, :description, :active) " +
                "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, active = EXCLUDED.active"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct upsert SQL for PostgreSQL with multiple key columns`() {
        // Given
        val tableName = "test_table"

        val keyColumns = listOf(ColumnInfo("id", "id"),ColumnInfo("code", "code"))
        val valueColumns = listOf(ColumnInfo("name", "name"), ColumnInfo("description", "description"), ColumnInfo("active", "active"))

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, 1)

        // Then
        val expectedSql = "INSERT INTO test_table (id, code, name, description, active) VALUES (:id, :code, :name, :description, :active) " +
                "ON CONFLICT (id, code) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, active = EXCLUDED.active"
        assertEquals(expectedSql, sql)
    }
}
