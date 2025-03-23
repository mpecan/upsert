package io.github.mpecan.upsert.integration.base

import io.github.mpecan.upsert.entity.JpaTestEntity
import io.github.mpecan.upsert.integration.TestApplication
import io.github.mpecan.upsert.integration.repositories.CustomMethodsTestRepository
import io.github.mpecan.upsert.integration.repositories.JpaTestEntityRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Integration tests for PostgreSQL UpsertRepository implementation.
 */
@SpringBootTest(classes = [TestApplication::class])
@Testcontainers
abstract class AbstractRepositoryIntegrationTest {

    private val logger = LoggerFactory.getLogger(AbstractRepositoryIntegrationTest::class.java)

    @Autowired
    private lateinit var jpaTestEntityRepository: JpaTestEntityRepository

    @Autowired
    private lateinit var customMethodsTestRepository: CustomMethodsTestRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        try {
            // Clear the tables before each test
            jdbcTemplate.execute("DELETE FROM jpa_test_entity")
            logger.info("Tables cleared successfully")
        } catch (e: Exception) {
            logger.error("Error clearing tables", e)
            throw e
        }
    }

    @Test
    fun `should insert new jpa entity using repository`() {
        // Given
        val id: Long = 3
        val entity = JpaTestEntity(id, "JPA Test Entity", "JPA Test Description", true)

        // When
        val updatedEntity = jpaTestEntityRepository.upsert(entity)

        // Then
        Assertions.assertEquals(id, updatedEntity.id)
        Assertions.assertEquals("JPA Test Entity", updatedEntity.name)
        Assertions.assertEquals("JPA Test Description", updatedEntity.description)
        Assertions.assertEquals(true, updatedEntity.active)

        // Verify the entity was inserted

        val result = jpaTestEntityRepository.findById(id).get()
        Assertions.assertEquals(3L, result.id)
        Assertions.assertEquals("JPA Test Entity", result.name)
        Assertions.assertEquals("JPA Test Description", result.description)
        Assertions.assertEquals(true, result.active)
    }

    @Test
    fun `should update existing jpa entity using repository`() {
        // Given
        val entity1 = JpaTestEntity(4, "Original JPA Entity", "Original JPA Description", true)
        val entity2 = JpaTestEntity(4, "Updated JPA Entity", "Updated JPA Description", false)

        // Insert the original entity
        jpaTestEntityRepository.upsert(entity1)

        // When
        val updatedEntity = jpaTestEntityRepository.upsert(entity2)

        // Then
        Assertions.assertEquals(4, updatedEntity.id)
        Assertions.assertEquals("Updated JPA Entity", updatedEntity.name)
        Assertions.assertEquals("Updated JPA Description", updatedEntity.description)
        Assertions.assertEquals(false, updatedEntity.active)

        // Verify the entity was updated
        val result = jpaTestEntityRepository.findById(4).get()
        Assertions.assertEquals(4L, result.id)
        Assertions.assertEquals("Updated JPA Entity", result.name)
        Assertions.assertEquals("Updated JPA Description", result.description)
        Assertions.assertEquals(false, result.active)
    }

    @Test
    fun `should insert multiple new jpa entities using repository`() {
        // Given
        val entities = listOf(
            JpaTestEntity(5, "JPA Entity 1", "Description 1", true),
            JpaTestEntity(6, "JPA Entity 2", "Description 2", false),
            JpaTestEntity(7, "JPA Entity 3", "Description 3", true)
        )

        // When
        val updatedEntities = jpaTestEntityRepository.upsertAll(entities)

        // Then
        Assertions.assertEquals(3, updatedEntities.size)

        // Verify the returned entities
        Assertions.assertEquals(5, updatedEntities[0].id)
        Assertions.assertEquals("JPA Entity 1", updatedEntities[0].name)
        Assertions.assertEquals("Description 1", updatedEntities[0].description)
        Assertions.assertEquals(true, updatedEntities[0].active)

        Assertions.assertEquals(6, updatedEntities[1].id)
        Assertions.assertEquals("JPA Entity 2", updatedEntities[1].name)
        Assertions.assertEquals("Description 2", updatedEntities[1].description)
        Assertions.assertEquals(false, updatedEntities[1].active)

        Assertions.assertEquals(7, updatedEntities[2].id)
        Assertions.assertEquals("JPA Entity 3", updatedEntities[2].name)
        Assertions.assertEquals("Description 3", updatedEntities[2].description)
        Assertions.assertEquals(true, updatedEntities[2].active)

        // Verify the entities were inserted
        val results = jpaTestEntityRepository.findAll().toList()
        Assertions.assertEquals(3, results.size)

        Assertions.assertEquals(5L, results[0].id)
        Assertions.assertEquals("JPA Entity 1", results[0].name)
        Assertions.assertEquals("Description 1", results[0].description)
        Assertions.assertEquals(true, results[0].active)

        Assertions.assertEquals(6L, results[1].id)
        Assertions.assertEquals("JPA Entity 2", results[1].name)
        Assertions.assertEquals("Description 2", results[1].description)
        Assertions.assertEquals(false, results[1].active)

        Assertions.assertEquals(7L, results[2].id)
        Assertions.assertEquals("JPA Entity 3", results[2].name)
        Assertions.assertEquals("Description 3", results[2].description)
        Assertions.assertEquals(true, results[2].active)
    }

    @Test
    fun `should update multiple existing jpa entities using repository`() {
        // Given
        val originalEntities = listOf(
            JpaTestEntity(8, "Original Entity 1", "Original Description 1", true),
            JpaTestEntity(9, "Original Entity 2", "Original Description 2", true)
        )

        val updatedEntities = listOf(
            JpaTestEntity(8, "Updated Entity 1", "Updated Description 1", false),
            JpaTestEntity(9, "Updated Entity 2", "Updated Description 2", false)
        )

        // Insert the original entities
        jpaTestEntityRepository.upsertAll(originalEntities)

        // When
        val returnedEntities = jpaTestEntityRepository.upsertAll(updatedEntities)

        // Then
        Assertions.assertEquals(2, returnedEntities.size)

        // Verify the returned entities
        Assertions.assertEquals(8, returnedEntities[0].id)
        Assertions.assertEquals("Updated Entity 1", returnedEntities[0].name)
        Assertions.assertEquals("Updated Description 1", returnedEntities[0].description)
        Assertions.assertEquals(false, returnedEntities[0].active)

        Assertions.assertEquals(9, returnedEntities[1].id)
        Assertions.assertEquals("Updated Entity 2", returnedEntities[1].name)
        Assertions.assertEquals("Updated Description 2", returnedEntities[1].description)
        Assertions.assertEquals(false, returnedEntities[1].active)

        // Verify the entities were updated
        val results = jpaTestEntityRepository.findAll().toList()
        Assertions.assertEquals(2, results.size)

        Assertions.assertEquals(8L, results[0].id)
        Assertions.assertEquals("Updated Entity 1", results[0].name)
        Assertions.assertEquals("Updated Description 1", results[0].description)
        Assertions.assertEquals(false, results[0].active)

        Assertions.assertEquals(9L, results[1].id)
        Assertions.assertEquals("Updated Entity 2", results[1].name)
        Assertions.assertEquals("Updated Description 2", results[1].description)
        Assertions.assertEquals(false, results[1].active)
    }

    @Test
    fun `should handle mix of new and existing jpa entities using repository`() {
        // Given
        // Insert an entity that will be updated
        jpaTestEntityRepository.upsert(
            JpaTestEntity(
                10,
                "Original Entity",
                "Original Description",
                true
            )
        )

        val mixedEntities = listOf(
            JpaTestEntity(
                10,
                "Updated Entity",
                "Updated Description",
                false
            ), // Existing entity to update
            JpaTestEntity(
                11,
                "New Entity",
                "New Description",
                true
            )          // New entity to insert
        )

        // When
        val returnedEntities = jpaTestEntityRepository.upsertAll(mixedEntities)

        // Then
        Assertions.assertEquals(2, returnedEntities.size)

        // Verify the returned entities
        Assertions.assertEquals(10, returnedEntities[0].id)
        Assertions.assertEquals("Updated Entity", returnedEntities[0].name)
        Assertions.assertEquals("Updated Description", returnedEntities[0].description)
        Assertions.assertEquals(false, returnedEntities[0].active)

        Assertions.assertEquals(11, returnedEntities[1].id)
        Assertions.assertEquals("New Entity", returnedEntities[1].name)
        Assertions.assertEquals("New Description", returnedEntities[1].description)
        Assertions.assertEquals(true, returnedEntities[1].active)

        // Verify the entities
        val results = jpaTestEntityRepository.findAll().toList()
        Assertions.assertEquals(2, results.size)

        Assertions.assertEquals(10L, results[0].id)
        Assertions.assertEquals("Updated Entity", results[0].name)
        Assertions.assertEquals("Updated Description", results[0].description)
        Assertions.assertEquals(false, results[0].active)

        Assertions.assertEquals(11L, results[1].id)
        Assertions.assertEquals("New Entity", results[1].name)
        Assertions.assertEquals("New Description", results[1].description)
        Assertions.assertEquals(true, results[1].active)
    }

    @Test
    fun `should handle jpa entity with null description`() {
        // Given
        val entity = JpaTestEntity(12, "Entity With Null Description", null, true)

        // When
        val updatedEntity = jpaTestEntityRepository.upsert(entity)

        // Then
        Assertions.assertEquals(12, updatedEntity.id)
        Assertions.assertEquals("Entity With Null Description", updatedEntity.name)
        Assertions.assertEquals(null, updatedEntity.description)
        Assertions.assertEquals(true, updatedEntity.active)

        // Verify the entity was inserted with null description
        val result = jpaTestEntityRepository.findById(12).get()
        Assertions.assertEquals(12L, result.id)
        Assertions.assertEquals("Entity With Null Description", result.name)
        Assertions.assertEquals(null, result.description)
        Assertions.assertEquals(true, result.active)
    }

    @Test
    fun `should handle empty list for upsertAll`() {
        // Given
        val emptyList = emptyList<JpaTestEntity>()

        // When
        val returnedEntities = jpaTestEntityRepository.upsertAll(emptyList)

        // Then
        Assertions.assertTrue(returnedEntities.isEmpty())
    }

    /**
     * Tests for custom method functionality.
     * These tests verify that the UpsertMethodNameParser and UpsertMethodInvoker work correctly.
     */

    @Test
    fun `should parse upsert method names correctly`() {
        // Create a parser
        val parser = io.github.mpecan.upsert.repository.UpsertMethodNameParser()

        // Test parsing "upsertOnName"
        val info1 = parser.parse("upsertOnName")
        Assertions.assertNotNull(info1)
        Assertions.assertEquals(false, info1!!.isUpsertAll)
        Assertions.assertEquals(listOf("name"), info1.onFields)
        Assertions.assertEquals(emptyList<String>(), info1.ignoredFields)
        Assertions.assertEquals(false, info1.ignoreAllFields)

        // Test parsing "upsertOnNameIgnoringActive"
        val info2 = parser.parse("upsertOnNameIgnoringActive")
        Assertions.assertNotNull(info2)
        Assertions.assertEquals(false, info2!!.isUpsertAll)
        Assertions.assertEquals(listOf("name"), info2.onFields)
        Assertions.assertEquals(listOf("active"), info2.ignoredFields)
        Assertions.assertEquals(false, info2.ignoreAllFields)

        // Test parsing "upsertOnNameAndDescriptionIgnoringActive"
        val info3 = parser.parse("upsertOnNameAndDescriptionIgnoringActive")
        Assertions.assertNotNull(info3)
        Assertions.assertEquals(false, info3!!.isUpsertAll)
        Assertions.assertEquals(listOf("name", "description"), info3.onFields)
        Assertions.assertEquals(listOf("active"), info3.ignoredFields)
        Assertions.assertEquals(false, info3.ignoreAllFields)

        // Test parsing "upsertOnNameIgnoringAllFields"
        val info4 = parser.parse("upsertOnNameIgnoringAllFields")
        Assertions.assertNotNull(info4)
        Assertions.assertEquals(false, info4!!.isUpsertAll)
        Assertions.assertEquals(listOf("name"), info4.onFields)
        Assertions.assertEquals(emptyList<String>(), info4.ignoredFields)
        Assertions.assertEquals(true, info4.ignoreAllFields)

        // Test parsing "upsertAllOnName"
        val info5 = parser.parse("upsertAllOnName")
        Assertions.assertNotNull(info5)
        Assertions.assertEquals(true, info5!!.isUpsertAll)
        Assertions.assertEquals(listOf("name"), info5.onFields)
        Assertions.assertEquals(emptyList<String>(), info5.ignoredFields)
        Assertions.assertEquals(false, info5.ignoreAllFields)
    }


    @Test
    fun `should handle custom upsert operations using reflection`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(1, "Test Entity", "Updated Description", false)

        // Insert the first entity
        jpaTestEntityRepository.upsert(entity1)

        // When - upsert with the same name but different ID
        val updatedEntity = customMethodsTestRepository.upsertOnId(entity2)

        // Then
        Assertions.assertEquals(1, updatedEntity.id)
        Assertions.assertEquals("Test Entity", updatedEntity.name)
        Assertions.assertEquals("Updated Description", updatedEntity.description)
        Assertions.assertEquals(false, updatedEntity.active)

        // Verify the entity was updated
        val result = jpaTestEntityRepository.findById(1).get()
        Assertions.assertEquals(1L, result.id)
        Assertions.assertEquals("Test Entity", result.name)
        Assertions.assertEquals("Updated Description", result.description)
        Assertions.assertEquals(false, result.active)
    }

    @Test
    fun `should throw exception if custom operations do not include columns with a uniqueness constraint`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Updated Description", false)

        // Insert the first entity
        jpaTestEntityRepository.upsert(entity1)


        // When - upsert with the same name but different ID
        val exception = Assertions.assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertOnName(entity2)
        }

        // Then

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
    }

    @Test
    fun `should not update existing entity when using ignoreAllFields`() {
        // Given
        val entity1 = JpaTestEntity(20, "Unique Name", "Original Description", true)
        val entity2 = JpaTestEntity(20, "Updated Name", "Updated Description", false)

        // Insert the first entity
        jpaTestEntityRepository.upsert(entity1)

        // When - upsert with the same id but different name, description and active status
        val result = customMethodsTestRepository.upsertOnIdIgnoringAllFields(entity2)

        // Then
        Assertions.assertEquals(20, result.id)
        Assertions.assertEquals("Updated Name", result.name)

        // Verify the entity was NOT updated in the database
        val databaseEntity = jpaTestEntityRepository.findById(20).get()
        Assertions.assertEquals(20L, databaseEntity.id)
        Assertions.assertEquals("Unique Name", databaseEntity.name)
        Assertions.assertEquals("Original Description", databaseEntity.description)
        Assertions.assertEquals(true, databaseEntity.active)
    }

    @Test
    fun `should insert new entity when using ignoreAllFields`() {
        // Given
        val entity = JpaTestEntity(21, "New Unique Name", "New Description", true)

        // When - upsert a new entity
        val result = customMethodsTestRepository.upsertOnIdIgnoringAllFields(entity)

        // Then
        Assertions.assertEquals(21, result.id)
        Assertions.assertEquals("New Unique Name", result.name)
        Assertions.assertEquals("New Description", result.description)
        Assertions.assertEquals(true, result.active)

        // Verify the entity was inserted in the database
        val databaseEntity = jpaTestEntityRepository.findById(21).get()
        Assertions.assertEquals(21L, databaseEntity.id)
        Assertions.assertEquals("New Unique Name", databaseEntity.name)
        Assertions.assertEquals("New Description", databaseEntity.description)
        Assertions.assertEquals(true, databaseEntity.active)
    }


}