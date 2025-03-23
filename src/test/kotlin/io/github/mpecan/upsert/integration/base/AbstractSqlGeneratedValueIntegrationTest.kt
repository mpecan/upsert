package io.github.mpecan.upsert.integration.base

import io.github.mpecan.upsert.entity.JpaTestEntityWithGeneratedId
import io.github.mpecan.upsert.integration.TestApplication
import io.github.mpecan.upsert.integration.repositories.JpaTestEntityWithGeneratedIdRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Integration tests for entities with @GeneratedValue annotations.
 * These tests verify that the upsert operations work correctly with entities that have
 * auto-generated IDs (the field should be nullable and a var).
 */
@SpringBootTest(classes = [TestApplication::class])
@Testcontainers
@Transactional
abstract class AbstractSqlGeneratedValueIntegrationTest {

    private val logger =
        LoggerFactory.getLogger(AbstractSqlGeneratedValueIntegrationTest::class.java)

    @Autowired
    private lateinit var jpaTestEntityWithGeneratedIdRepository: JpaTestEntityWithGeneratedIdRepository

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        // The table will be created by Hibernate based on the entity class
        // We don't need to manually clear it as we're using ddl-auto=create-drop
        createTable()
        logger.info("Setup completed")
    }

    protected abstract fun createTable()

    @Test
    fun `should insert new entity with generated id`() {
        // Given
        val entity = JpaTestEntityWithGeneratedId(
            name = "Test Entity",
            description = "Test Description",
            active = true
        )

        // When
        val result = jpaTestEntityWithGeneratedIdRepository.upsert(entity)

        // Then
        Assertions.assertNotNull(result.id)
        Assertions.assertEquals("Test Entity", result.name)
        Assertions.assertEquals("Test Description", result.description)
        Assertions.assertEquals(true, result.active)
    }

    @Test
    fun `should update existing entity with generated id`() {
        // Given
        // First, insert an entity
        val entity1 = JpaTestEntityWithGeneratedId(
            name = "Original Entity",
            description = "Original Description",
            active = true
        )
        val insertedEntity = jpaTestEntityWithGeneratedIdRepository.upsert(entity1)

        // Now create an updated entity with the same ID
        val entity2 = JpaTestEntityWithGeneratedId(
            id = insertedEntity.id,
            name = "Updated Entity",
            description = "Updated Description",
            active = false
        )

        // When
        val result = jpaTestEntityWithGeneratedIdRepository.upsert(entity2)

        // Then
        Assertions.assertEquals(insertedEntity.id, result.id)
        Assertions.assertEquals("Updated Entity", result.name)
        Assertions.assertEquals("Updated Description", result.description)
        Assertions.assertEquals(false, result.active)
    }

    @Test
    fun `should insert multiple new entities with generated ids`() {
        // Given
        val entities = listOf(
            JpaTestEntityWithGeneratedId(
                name = "Entity 1",
                description = "Description 1",
                active = true
            ),
            JpaTestEntityWithGeneratedId(
                name = "Entity 2",
                description = "Description 2",
                active = false
            ),
            JpaTestEntityWithGeneratedId(
                name = "Entity 3",
                description = "Description 3",
                active = true
            )
        )

        // When
        val results = jpaTestEntityWithGeneratedIdRepository.upsertAll(entities)

        // Then
        Assertions.assertEquals(3, results.size)

        Assertions.assertNotNull(results[0].id)
        Assertions.assertEquals("Entity 1", results[0].name)
        Assertions.assertEquals("Description 1", results[0].description)
        Assertions.assertEquals(true, results[0].active)

        Assertions.assertNotNull(results[1].id)
        Assertions.assertEquals("Entity 2", results[1].name)
        Assertions.assertEquals("Description 2", results[1].description)
        Assertions.assertEquals(false, results[1].active)

        Assertions.assertNotNull(results[2].id)
        Assertions.assertEquals("Entity 3", results[2].name)
        Assertions.assertEquals("Description 3", results[2].description)
        Assertions.assertEquals(true, results[2].active)
    }

    @Test
    fun `should update existing entities with generated ids`() {
        // Given
        // First, insert some entities
        val originalEntities = listOf(
            JpaTestEntityWithGeneratedId(
                name = "Original Entity 1",
                description = "Original Description 1",
                active = true
            ),
            JpaTestEntityWithGeneratedId(
                name = "Original Entity 2",
                description = "Original Description 2",
                active = true
            )
        )
        val insertedEntities = jpaTestEntityWithGeneratedIdRepository.upsertAll(originalEntities)

        // Now create updated entities with the same IDs
        val updatedEntities = listOf(
            JpaTestEntityWithGeneratedId(
                id = insertedEntities[0].id,
                name = "Updated Entity 1",
                description = "Updated Description 1",
                active = false
            ),
            JpaTestEntityWithGeneratedId(
                id = insertedEntities[1].id,
                name = "Updated Entity 2",
                description = "Updated Description 2",
                active = false
            )
        )

        // When
        val results = jpaTestEntityWithGeneratedIdRepository.upsertAll(updatedEntities)

        // Then
        Assertions.assertEquals(2, results.size)

        Assertions.assertEquals(insertedEntities[0].id, results[0].id)
        Assertions.assertEquals("Updated Entity 1", results[0].name)
        Assertions.assertEquals("Updated Description 1", results[0].description)
        Assertions.assertEquals(false, results[0].active)

        Assertions.assertEquals(insertedEntities[1].id, results[1].id)
        Assertions.assertEquals("Updated Entity 2", results[1].name)
        Assertions.assertEquals("Updated Description 2", results[1].description)
        Assertions.assertEquals(false, results[1].active)
    }

    @Test
    fun `should handle mix of new and existing entities with generated ids`() {
        // Given
        // First, insert an entity
        val originalEntity = JpaTestEntityWithGeneratedId(
            name = "Original Entity",
            description = "Original Description",
            active = true
        )
        val insertedEntity = jpaTestEntityWithGeneratedIdRepository.upsert(originalEntity)

        // Now create a mix of updated and new entities
        val mixedEntities = listOf(
            JpaTestEntityWithGeneratedId(
                id = insertedEntity.id,
                name = "Updated Entity",
                description = "Updated Description",
                active = false
            ),
            JpaTestEntityWithGeneratedId(
                name = "New Entity",
                description = "New Description",
                active = true
            )
        )

        // When
        val results = jpaTestEntityWithGeneratedIdRepository.upsertAll(mixedEntities)

        // Then
        Assertions.assertEquals(2, results.size)

        Assertions.assertEquals(insertedEntity.id, results[0].id)
        Assertions.assertEquals("Updated Entity", results[0].name)
        Assertions.assertEquals("Updated Description", results[0].description)
        Assertions.assertEquals(false, results[0].active)

        Assertions.assertNotNull(results[1].id)
        Assertions.assertEquals("New Entity", results[1].name)
        Assertions.assertEquals("New Description", results[1].description)
        Assertions.assertEquals(true, results[1].active)
    }

    @Test
    fun `should handle entity with null description`() {
        // Given
        val entity = JpaTestEntityWithGeneratedId(
            name = "Entity With Null Description",
            description = null,
            active = true
        )

        // When
        val result = jpaTestEntityWithGeneratedIdRepository.upsert(entity)

        // Then
        Assertions.assertNotNull(result.id)
        Assertions.assertEquals("Entity With Null Description", result.name)
        Assertions.assertEquals(null, result.description)
        Assertions.assertEquals(true, result.active)
    }

    @Test
    fun `should handle empty list for upsertAll`() {
        // Given
        val emptyList = emptyList<JpaTestEntityWithGeneratedId>()

        // When
        val results = jpaTestEntityWithGeneratedIdRepository.upsertAll(emptyList)

        // Then
        Assertions.assertEquals(0, results.size)
    }
}