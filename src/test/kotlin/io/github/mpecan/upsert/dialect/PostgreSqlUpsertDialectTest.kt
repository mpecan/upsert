package io.github.mpecan.upsert.dialect

import io.github.mpecan.upsert.model.ColumnInfo
import io.github.mpecan.upsert.type.TypeMapperRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for PostgreSqlUpsertDialect.
 */
class PostgreSqlUpsertDialectTest {

    private val typeMapperRegistry = TypeMapperRegistry(testTypeProvider())
    private val dialect = PostgreSqlUpsertDialect(typeMapperRegistry)

    @Test
    fun `should generate correct upsert SQL for PostgreSQL`() {
        // Given
        val tableName = "test_table"

        val keyColumns = listOf(ColumnInfo("id", "id", Int::class.java, 4, false))
        val valueColumns = listOf(
            ColumnInfo("name", "name", String::class.java, 12),
            ColumnInfo("description", "description", String::class.java, 12, false),
            ColumnInfo("active", "active", Boolean::class.java, 16, false)
        )

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, keyColumns+  valueColumns, valueColumns,1)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, name, description, active) VALUES (:id, :name, :description, :active) " +
                    "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, active = EXCLUDED.active"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct upsert SQL for PostgreSQL with multiple key columns`() {
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
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, keyColumns + valueColumns, valueColumns, 1)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, code, name, description, active) VALUES (:id, :code, :name, :description, :active) " +
                    "ON CONFLICT (id, code) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, active = EXCLUDED.active"
        assertEquals(expectedSql, sql)
    }
}
