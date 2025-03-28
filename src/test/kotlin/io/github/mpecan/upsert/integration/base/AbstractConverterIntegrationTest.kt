package io.github.mpecan.upsert.integration.base

import io.github.mpecan.upsert.entity.JpaTestEntityWithConverter
import io.github.mpecan.upsert.entity.JsonData
import io.github.mpecan.upsert.integration.TestApplication
import io.github.mpecan.upsert.integration.repositories.JpaTestEntityWithConverterRepository
import io.github.mpecan.upsert.integration.repositories.TestJsonEntityRepository
import io.github.mpecan.upsert.type.json.test.TestJsonEntity
import io.github.mpecan.upsert.type.json.test.TestMetadata
import io.github.mpecan.upsert.type.json.test.TestNestedData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.ResultSet
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Abstract integration tests for upsert operations with custom converters.
 */
@SpringBootTest(classes = [TestApplication::class])
@Testcontainers
abstract class AbstractConverterIntegrationTest {

    private val logger = LoggerFactory.getLogger(AbstractConverterIntegrationTest::class.java)

    @Autowired
    protected lateinit var jpaTestEntityWithConverterRepository: JpaTestEntityWithConverterRepository

    @Autowired
    protected lateinit var jsonTestEntityRepository: TestJsonEntityRepository

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        try {
            // Clear the tables before each test
            jdbcTemplate.execute("DELETE FROM jpa_test_entity_with_converter")
            setupTables()
            logger.info("Tables cleared successfully")
        } catch (e: Exception) {
            logger.error("Error clearing tables", e)
            throw e
        }
    }

    abstract fun setupTables()

    @Test
    fun `test upsert with custom converter`() {
        // Given
        val entity = JpaTestEntityWithConverter(
            id = 1L,
            name = "Test Entity",
            jsonData = JsonData("test-key", "test-value"),
            active = true
        )

        // When
        val result = jpaTestEntityWithConverterRepository.upsert(entity)

        // Then
        Assertions.assertEquals(1L, result.id)
        Assertions.assertEquals("Test Entity", result.name)
        Assertions.assertEquals("test-key", result.jsonData.key)
        Assertions.assertEquals("test-value", result.jsonData.value)
        Assertions.assertEquals(true, result.active)

        // Verify the data was correctly stored in the database
        val storedEntity = jdbcTemplate.queryForObject(
            "SELECT * FROM jpa_test_entity_with_converter WHERE id = ?",
            entityRowMapper(),
            1L
        )

        Assertions.assertEquals(1L, storedEntity?.id)
        Assertions.assertEquals("Test Entity", storedEntity?.name)
        Assertions.assertEquals("test-key", storedEntity?.jsonData?.key)
        Assertions.assertEquals("test-value", storedEntity?.jsonData?.value)
        Assertions.assertEquals(true, storedEntity?.active)
    }

    @Test
    fun `test upsert update with custom converter`() {
        // Given
        // First insert an entity
        val entity1 = JpaTestEntityWithConverter(
            id = 2L,
            name = "Original Entity",
            jsonData = JsonData("original-key", "original-value"),
            active = true
        )
        jpaTestEntityWithConverterRepository.upsert(entity1)

        // Now create an updated entity with the same ID
        val entity2 = JpaTestEntityWithConverter(
            id = 2L,
            name = "Updated Entity",
            jsonData = JsonData("updated-key", "updated-value"),
            active = false
        )

        // When
        val result = jpaTestEntityWithConverterRepository.upsert(entity2)

        // Then
        Assertions.assertEquals(2L, result.id)
        Assertions.assertEquals("Updated Entity", result.name)
        Assertions.assertEquals("updated-key", result.jsonData.key)
        Assertions.assertEquals("updated-value", result.jsonData.value)
        Assertions.assertEquals(false, result.active)

        // Verify the data was correctly updated in the database
        val storedEntity = jdbcTemplate.queryForObject(
            "SELECT * FROM jpa_test_entity_with_converter WHERE id = ?",
            entityRowMapper(),
            2L
        )

        Assertions.assertEquals(2L, storedEntity?.id)
        Assertions.assertEquals("Updated Entity", storedEntity?.name)
        Assertions.assertEquals("updated-key", storedEntity?.jsonData?.key)
        Assertions.assertEquals("updated-value", storedEntity?.jsonData?.value)
        Assertions.assertEquals(false, storedEntity?.active)
    }

    @Test
    fun testUpsertEntityWithJsonFields() {
        // Create test entity with JSON fields
        val entity = TestJsonEntity(
            name = "Test JSON Entity",
            attributes = mapOf("key1" to "value1", "key2" to "value2"),
            tags = listOf("tag1", "tag2", "tag3"),
            metadata = TestMetadata(
                version = "1.0.0",
                description = "Test metadata",
                active = true,
                settings = mapOf("setting1" to "value1"),
                scores = listOf(10, 20, 30),
                nested = TestNestedData("nested1", 42.5)
            ),
            createdAt = LocalDateTime.now()
        )

        // Upsert the entity
        val savedEntity = jsonTestEntityRepository.upsert(entity)

        // Verify entity was saved
        assertNotNull(savedEntity.id)

        // Query the database directly to verify JSON was stored correctly
        val dbRow = jdbcTemplate.queryForMap(
            "SELECT * FROM test_json_entity WHERE id = ?",
            savedEntity.id
        )

        // Verify the data
        assertEquals("Test JSON Entity", dbRow["name"])

        // Verify JSON fields were saved as JSON
        val attributesJson = dbRow["attributes"].toString()
        assertTrue(attributesJson.contains("key1"))
        assertTrue(attributesJson.contains("value1"))

        val tagsJson = dbRow["tags"].toString()
        assertTrue(tagsJson.contains("tag1"))

        val metadataJson = dbRow["metadata"].toString()
        assertTrue(metadataJson.contains("version"))
        assertTrue(metadataJson.contains("1.0.0"))
        assertTrue(metadataJson.contains("nested"))

        // Update the entity
        val updatedMetadata = savedEntity.metadata.copy(
            version = "1.1.0",
            settings = mapOf("setting1" to "updated", "setting2" to "new value")
        )
        val updatedEntity = savedEntity.copy(
            metadata = updatedMetadata,
            tags = savedEntity.tags + "tag4"
        )

        // Upsert the updated entity
        val reUpdatedEntity = jsonTestEntityRepository.upsert(updatedEntity)

        // Query the database again
        val updatedDbRow = jdbcTemplate.queryForMap(
            "SELECT * FROM test_json_entity WHERE id = ?",
            reUpdatedEntity.id!!
        )

        // Verify updates were saved
        val updatedMetadataJson = updatedDbRow["metadata"].toString()
        assertTrue(updatedMetadataJson.contains("1.1.0"))
        assertTrue(updatedMetadataJson.contains("setting2"))
        assertTrue(updatedMetadataJson.contains("new value"))

        val updatedTagsJson = updatedDbRow["tags"].toString()
        assertTrue(updatedTagsJson.contains("tag4"))
    }

    private fun entityRowMapper(): RowMapper<JpaTestEntityWithConverter> {
        return RowMapper { rs: ResultSet, _: Int ->
            val id = rs.getLong("id")
            val name = rs.getString("name")
            val jsonDataStr = rs.getString("json_data")
            val jsonData = io.github.mpecan.upsert.entity.JsonDataConverter()
                .convertToEntityAttribute(jsonDataStr)
            val active = rs.getBoolean("active")

            JpaTestEntityWithConverter(id, name, jsonData!!, active)
        }
    }
}