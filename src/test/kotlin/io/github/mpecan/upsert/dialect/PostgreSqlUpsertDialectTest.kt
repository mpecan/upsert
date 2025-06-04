package io.github.mpecan.upsert.dialect

import io.github.mpecan.upsert.model.ColumnInfo
import io.github.mpecan.upsert.model.ComparisonOperator
import io.github.mpecan.upsert.model.ConditionalInfo
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

    @Test
    fun `should generate correct conditional upsert SQL for PostgreSQL with More operator`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf(ColumnInfo("id", "id", Int::class.java, 4, false))
        val valueColumns = listOf(
            ColumnInfo("id", "id", Int::class.java, 4, false),
            ColumnInfo("name", "name", String::class.java, 12),
            ColumnInfo("updated_at", "updatedAt", Long::class.java, -5)
        )
        val updateColumns = listOf(
            ColumnInfo("name", "name", String::class.java, 12),
            ColumnInfo("updated_at", "updatedAt", Long::class.java, -5)
        )
        val conditionalInfo = ConditionalInfo("updated_at", ComparisonOperator.MORE)

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, name, updated_at) VALUES (:id, :name, :updatedAt) " +
                    "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = EXCLUDED.updated_at WHERE EXCLUDED.updated_at > test_table.updated_at"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct conditional upsert SQL for PostgreSQL with MoreOrEqual operator`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf(ColumnInfo("id", "id", Int::class.java, 4, false))
        val valueColumns = listOf(
            ColumnInfo("id", "id", Int::class.java, 4, false),
            ColumnInfo("version", "version", Int::class.java, 4)
        )
        val updateColumns = listOf(ColumnInfo("version", "version", Int::class.java, 4))
        val conditionalInfo = ConditionalInfo("version", ComparisonOperator.MORE_OR_EQUAL)

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, version) VALUES (:id, :version) " +
                    "ON CONFLICT (id) DO UPDATE SET version = EXCLUDED.version WHERE EXCLUDED.version >= test_table.version"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct conditional upsert SQL for PostgreSQL with Less operator`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf(ColumnInfo("id", "id", Int::class.java, 4, false))
        val valueColumns = listOf(
            ColumnInfo("id", "id", Int::class.java, 4, false),
            ColumnInfo("price", "price", Double::class.java, 8)
        )
        val updateColumns = listOf(ColumnInfo("price", "price", Double::class.java, 8))
        val conditionalInfo = ConditionalInfo("price", ComparisonOperator.LESS)

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, price) VALUES (:id, :price) " +
                    "ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price WHERE EXCLUDED.price < test_table.price"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct conditional upsert SQL for PostgreSQL with LessOrEqual operator`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf(ColumnInfo("id", "id", Int::class.java, 4, false))
        val valueColumns = listOf(
            ColumnInfo("id", "id", Int::class.java, 4, false),
            ColumnInfo("score", "score", Float::class.java, 7)
        )
        val updateColumns = listOf(ColumnInfo("score", "score", Float::class.java, 7))
        val conditionalInfo = ConditionalInfo("score", ComparisonOperator.LESS_OR_EQUAL)

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, score) VALUES (:id, :score) " +
                    "ON CONFLICT (id) DO UPDATE SET score = EXCLUDED.score WHERE EXCLUDED.score <= test_table.score"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct upsert SQL for PostgreSQL without conditional when no conditional info provided`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf(ColumnInfo("id", "id", Int::class.java, 4, false))
        val valueColumns = listOf(
            ColumnInfo("id", "id", Int::class.java, 4, false),
            ColumnInfo("name", "name", String::class.java, 12)
        )
        val updateColumns = listOf(ColumnInfo("name", "name", String::class.java, 12))

        // When
        val sql = dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, null)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, name) VALUES (:id, :name) " +
                    "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name"
        assertEquals(expectedSql, sql)
    }
}
