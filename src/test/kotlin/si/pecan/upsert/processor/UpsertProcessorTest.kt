package si.pecan.upsert.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import si.pecan.upsert.annotation.UpsertKey
import si.pecan.upsert.annotation.UpsertValue
import si.pecan.upsert.dialect.PostgreSqlUpsertDialect
import si.pecan.upsert.entity.JpaTestEntity
import si.pecan.upsert.entity.TestEntity

/**
 * Unit tests for UpsertProcessor.
 */
class UpsertProcessorTest {

    private val dialect = PostgreSqlUpsertDialect()
    private val processor = UpsertProcessor(dialect)

    @Test
    fun `should process entity class and generate SQL`() {
        // Given
        val tableName = "test_entity"

        // When
        val sql = processor.processUpsertEntity(TestEntity::class.java, tableName)

        // Then
        val expectedSql = "INSERT INTO test_entity (id, name, description, active) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, active = EXCLUDED.active"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should process JPA entity class and generate SQL`() {
        // Given
        val tableName = "jpa_test_entity"

        // When
        val sql = processor.processUpsertEntity(JpaTestEntity::class.java, tableName)

        // Then
        val expectedSql = "INSERT INTO jpa_test_entity (id, name, description, active) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, active = EXCLUDED.active"
        assertEquals(expectedSql, sql)
    }

    @Test
    fun `should throw exception when no key columns found`() {
        // Given
        class EntityWithNoKey(
            @UpsertValue
            val name: String
        )

        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            processor.processUpsertEntity(EntityWithNoKey::class.java, "entity_with_no_key")
        }
        assertEquals("No key fields found in ${EntityWithNoKey::class.java.name}. Use @UpsertKey, @Id, or @EmbeddedId annotations to mark key fields.", exception.message)
    }

    @Test
    fun `should throw exception when no value columns found`() {
        // Given
        class EntityWithNoValue(
            @UpsertKey
            val id: Long
        )

        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            processor.processUpsertEntity(EntityWithNoValue::class.java, "entity_with_no_value")
        }
        assertEquals("No value fields found in ${EntityWithNoValue::class.java.name}. Either use @UpsertValue annotations or ensure there are non-key fields in the entity.", exception.message)
    }
}
