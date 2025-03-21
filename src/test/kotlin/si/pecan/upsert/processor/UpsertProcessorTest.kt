package si.pecan.upsert.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import si.pecan.upsert.dialect.PostgreSqlUpsertDialect
import si.pecan.upsert.entity.JpaTestEntity
import jakarta.persistence.Id
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import si.pecan.upsert.dialect.ColumnInfo
import si.pecan.upsert.model.UpsertModel

/**
 * Unit tests for UpsertProcessor.
 */
class UpsertProcessorTest {

    private val dialect = PostgreSqlUpsertDialect()
    private val processor = UpsertProcessor(dialect)

    @Test
    fun `should process JPA entity class and generate SQL`() {
        // Given
        val tableName = "jpa_test_entity"

        val upsertModel = mock<UpsertModel>{
            on { getTableName() } doReturn tableName
            on { entityClass } doReturn JpaTestEntity::class.java
            on { getDefaultOnColumns() } doReturn listOf(
                ColumnInfo("id", "id", Long::class.java, 4, false)
            )
            on { getDefaultValues() } doReturn listOf(
                ColumnInfo("name", "name", String::class.java, 12),
                ColumnInfo("description", "description", String::class.java, 12, false),
                ColumnInfo("active", "active", Boolean::class.java, 16, false)
            )
        }

        // When
        val sql = processor.processUpsertEntity(upsertModel)

        // Then
        val expectedSql = "INSERT INTO jpa_test_entity (id, name, description, active) VALUES (:id, :name, :description, :active) " +
                "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, active = EXCLUDED.active"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should throw exception when no key columns found`() {
        // Given
        class EntityWithNoKey(
            val name: String
        )
        val upsertModel = mock<UpsertModel>{
            on { entityClass } doReturn EntityWithNoKey::class.java
            on {getTableName()} doReturn "entity_with_no_key"
            on { getDefaultOnColumns() } doReturn listOf(
                ColumnInfo("name", "name", String::class.java, 12)
            )
            on { getDefaultValues() } doReturn listOf(
                ColumnInfo("name", "name", String::class.java, 12)
            )
            on {validateUpsertQuery(any(), any(), any())} doThrow IllegalArgumentException("No key fields found in ${EntityWithNoKey::class.java.name}. Use @Id or @EmbeddedId annotations to mark key fields.")
        }

        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            processor.processUpsertEntity(upsertModel)
        }
        assertEquals("No key fields found in ${EntityWithNoKey::class.java.name}. Use @Id or @EmbeddedId annotations to mark key fields.", exception.message)
    }

    @Test
    fun `should throw exception when no value columns found`() {
        // Given
        class EntityWithNoValue(
            @Id
            val id: Long
        )

        val upsertModel = mock<UpsertModel>{
            on { entityClass } doReturn EntityWithNoValue::class.java
            on {getTableName()} doReturn "entity_with_no_value"
            on { getDefaultOnColumns() } doReturn listOf(
                ColumnInfo("id", "id", Long::class.java, 4, false)
            )
            on { getDefaultValues() } doReturn listOf()
            on {validateUpsertQuery(any(), any(), any())} doThrow IllegalArgumentException("No value fields found in ${EntityWithNoValue::class.java.name}. Ensure there are non-key fields in the entity.")
        }

        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            processor.processUpsertEntity(upsertModel)
        }
        assertEquals("No value fields found in ${EntityWithNoValue::class.java.name}. Ensure there are non-key fields in the entity.", exception.message)
    }
}
