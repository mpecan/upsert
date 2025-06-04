package io.github.mpecan.upsert.dialect

import io.github.mpecan.upsert.model.ColumnInfo
import io.github.mpecan.upsert.model.ComparisonOperator
import io.github.mpecan.upsert.model.ConditionalInfo
import io.github.mpecan.upsert.type.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.ObjectProvider

/**
 * Unit tests for MySqlUpsertDialect.
 */
class MySqlUpsertDialectTest {


    private val typeMapperRegistry = TypeMapperRegistry(testTypeProvider())
    private val modernDialect = MySqlUpsertDialect(typeMapperRegistry)
    private val legacyDialect = MySqlLegacyUpsertDialect(typeMapperRegistry)

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

        // When (test legacy dialect for basic functionality)
        val sql = legacyDialect.generateBatchUpsertSql(tableName, keyColumns, keyColumns + valueColumns, valueColumns, 1)

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
        val sql = legacyDialect.generateBatchUpsertSql(tableName, keyColumns, keyColumns + valueColumns, valueColumns,1)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, code, name, description, active) VALUES (:id_1, :code_1, :name_1, :description_1, :active_1) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), active = VALUES(active)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct conditional upsert SQL for MySQL with More operator`() {
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
        val sql = modernDialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, name, updated_at) VALUES (:id_1, :name_1, :updatedAt_1) AS new_values " +
                    "ON DUPLICATE KEY UPDATE name = IF(new_values.updated_at > test_table.updated_at, new_values.name, test_table.name), updated_at = IF(new_values.updated_at > test_table.updated_at, new_values.updated_at, test_table.updated_at)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct conditional upsert SQL for MySQL with MoreOrEqual operator`() {
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
        val sql = modernDialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, version) VALUES (:id_1, :version_1) AS new_values " +
                    "ON DUPLICATE KEY UPDATE version = IF(new_values.version >= test_table.version, new_values.version, test_table.version)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct conditional upsert SQL for MySQL with Less operator`() {
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
        val sql = modernDialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, price) VALUES (:id_1, :price_1) AS new_values " +
                    "ON DUPLICATE KEY UPDATE price = IF(new_values.price < test_table.price, new_values.price, test_table.price)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct conditional upsert SQL for MySQL with LessOrEqual operator`() {
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
        val sql = modernDialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, score) VALUES (:id_1, :score_1) AS new_values " +
                    "ON DUPLICATE KEY UPDATE score = IF(new_values.score <= test_table.score, new_values.score, test_table.score)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct upsert SQL for MySQL without conditional when no conditional info provided`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf(ColumnInfo("id", "id", Int::class.java, 4, false))
        val valueColumns = listOf(
            ColumnInfo("id", "id", Int::class.java, 4, false),
            ColumnInfo("name", "name", String::class.java, 12)
        )
        val updateColumns = listOf(ColumnInfo("name", "name", String::class.java, 12))

        // When
        val sql = legacyDialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, null)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, name) VALUES (:id_1, :name_1) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct conditional upsert SQL for legacy MySQL with More operator`() {
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
        val sql = legacyDialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, name, updated_at) VALUES (:id_1, :name_1, :updatedAt_1) " +
                    "ON DUPLICATE KEY UPDATE name = IF(VALUES(updated_at) > updated_at, VALUES(name), name), updated_at = IF(VALUES(updated_at) > updated_at, VALUES(updated_at), updated_at)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should generate correct conditional upsert SQL for legacy MySQL with MoreOrEqual operator`() {
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
        val sql = legacyDialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then
        val expectedSql =
            "INSERT INTO test_table (id, version) VALUES (:id_1, :version_1) " +
                    "ON DUPLICATE KEY UPDATE version = IF(VALUES(version) >= version, VALUES(version), version)"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should place conditional field last in update clause for both dialects`() {
        // Given
        val tableName = "test_table"
        val keyColumns = listOf(ColumnInfo("id", "id", Int::class.java, 4, false))
        val valueColumns = listOf(
            ColumnInfo("id", "id", Int::class.java, 4, false),
            ColumnInfo("name", "name", String::class.java, 12),
            ColumnInfo("description", "description", String::class.java, 12),
            ColumnInfo("version", "version", Int::class.java, 4)
        )
        val updateColumns = listOf(
            ColumnInfo("version", "version", Int::class.java, 4),
            ColumnInfo("name", "name", String::class.java, 12),
            ColumnInfo("description", "description", String::class.java, 12)
        )
        val conditionalInfo = ConditionalInfo("version", ComparisonOperator.MORE)

        // When - Modern dialect
        val modernSql = modernDialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)
        
        // When - Legacy dialect
        val legacySql = legacyDialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, updateColumns, 1, conditionalInfo)

        // Then - Modern dialect should have version field last
        val expectedModernSql =
            "INSERT INTO test_table (id, name, description, version) VALUES (:id_1, :name_1, :description_1, :version_1) AS new_values " +
                    "ON DUPLICATE KEY UPDATE name = IF(new_values.version > test_table.version, new_values.name, test_table.name), description = IF(new_values.version > test_table.version, new_values.description, test_table.description), version = IF(new_values.version > test_table.version, new_values.version, test_table.version)"
        assertEquals(expectedModernSql, modernSql)
        
        // Then - Legacy dialect should have version field last
        val expectedLegacySql =
            "INSERT INTO test_table (id, name, description, version) VALUES (:id_1, :name_1, :description_1, :version_1) " +
                    "ON DUPLICATE KEY UPDATE name = IF(VALUES(version) > version, VALUES(name), name), description = IF(VALUES(version) > version, VALUES(description), description), version = IF(VALUES(version) > version, VALUES(version), version)"
        assertEquals(expectedLegacySql, legacySql)
    }

    @Test
    fun `should generate correct insert clause for modern dialect with and without conditional`() {
        // Given
        val tableName = "test_table"
        val valueColumns = listOf(
            ColumnInfo("id", "id", Int::class.java, 4, false),
            ColumnInfo("name", "name", String::class.java, 12)
        )
        val allPlaceholders = "(:id_1, :name_1)"

        // When - with conditional
        val sqlWithConditional = modernDialect.generateInsertClause(tableName, valueColumns, allPlaceholders, true)
        
        // When - without conditional
        val sqlWithoutConditional = modernDialect.generateInsertClause(tableName, valueColumns, allPlaceholders, false)

        // Then
        val expectedWithConditional = "INSERT INTO test_table (id, name) VALUES (:id_1, :name_1) AS new_values"
        val expectedWithoutConditional = "INSERT INTO test_table (id, name) VALUES (:id_1, :name_1)"
        
        assertEquals(expectedWithConditional, sqlWithConditional)
        assertEquals(expectedWithoutConditional, sqlWithoutConditional)
    }

    @Test
    fun `should generate correct insert clause for legacy dialect regardless of conditional`() {
        // Given
        val tableName = "test_table"
        val valueColumns = listOf(
            ColumnInfo("id", "id", Int::class.java, 4, false),
            ColumnInfo("name", "name", String::class.java, 12)
        )
        val allPlaceholders = "(:id_1, :name_1)"

        // When - with conditional
        val sqlWithConditional = legacyDialect.generateInsertClause(tableName, valueColumns, allPlaceholders, true)
        
        // When - without conditional
        val sqlWithoutConditional = legacyDialect.generateInsertClause(tableName, valueColumns, allPlaceholders, false)

        // Then - Legacy dialect doesn't support aliases, so both should be the same
        val expected = "INSERT INTO test_table (id, name) VALUES (:id_1, :name_1)"
        
        assertEquals(expected, sqlWithConditional)
        assertEquals(expected, sqlWithoutConditional)
    }
}

fun testTypeProvider(vararg mappers: TypeMapper) = object : ObjectProvider<List<TypeMapper>> {
    override fun getObject(): List<TypeMapper> {
        return listOf(
            *mappers,
            NamedEnumTypeMapper(),
            EnumTypeMapper(),
            DefaultTypeMapper()
        )
    }
}