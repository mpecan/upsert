package si.pecan.upsert.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import si.pecan.upsert.entity.JpaTestEntityWithGeneratedId

/**
 * Integration tests for entities with @GeneratedValue annotations.
 * These tests verify that the upsert operations work correctly with entities that have
 * auto-generated IDs (the field should be nullable and a var).
 */
@SpringBootTest(classes = [TestApplication::class])
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MySqlGeneratedValueIntegrationTest {

    private val logger = LoggerFactory.getLogger(MySqlGeneratedValueIntegrationTest::class.java)

    companion object {
        @Container
        val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysqlContainer.jdbcUrl }
            registry.add("spring.datasource.username") { mysqlContainer.username }
            registry.add("spring.datasource.password") { mysqlContainer.password }
            registry.add("spring.datasource.driver-class-name") { mysqlContainer.driverClassName }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.MySQL8Dialect" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }

    @Autowired
    private lateinit var jpaTestEntityWithGeneratedIdRepository: JpaTestEntityWithGeneratedIdRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        // The table will be created by Hibernate based on the entity class
        // We don't need to manually clear it as we're using ddl-auto=create-drop
        logger.info("Setup completed")
    }

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
        assertNotNull(result.id)
        assertEquals("Test Entity", result.name)
        assertEquals("Test Description", result.description)
        assertEquals(true, result.active)
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
            name = "Original Entity",
            description = "Updated Description",
            active = false
        )

        // When
        val result = jpaTestEntityWithGeneratedIdRepository.upsert(entity2)

        // Then
        assertEquals(insertedEntity.id, result.id)
        assertEquals("Original Entity", result.name)
        assertEquals("Updated Description", result.description)
        assertEquals(false, result.active)
    }

    @Test
    fun `should insert multiple new entities with generated ids`() {
        // Given
        val entities = listOf(
            JpaTestEntityWithGeneratedId(name = "Entity 1", description = "Description 1", active = true),
            JpaTestEntityWithGeneratedId(name = "Entity 2", description = "Description 2", active = false),
            JpaTestEntityWithGeneratedId(name = "Entity 3", description = "Description 3", active = true)
        )

        // When
        val results = jpaTestEntityWithGeneratedIdRepository.upsertAll(entities)

        // Then
        assertEquals(3, results.size)

        assertNotNull(results[0].id)
        assertEquals("Entity 1", results[0].name)
        assertEquals("Description 1", results[0].description)
        assertEquals(true, results[0].active)

        assertNotNull(results[1].id)
        assertEquals("Entity 2", results[1].name)
        assertEquals("Description 2", results[1].description)
        assertEquals(false, results[1].active)

        assertNotNull(results[2].id)
        assertEquals("Entity 3", results[2].name)
        assertEquals("Description 3", results[2].description)
        assertEquals(true, results[2].active)
    }

    @Test
    fun `should update existing entities with generated ids`() {
        // Given
        // First, insert some entities
        val originalEntities = listOf(
            JpaTestEntityWithGeneratedId(name = "Original Entity 1", description = "Original Description 1", active = true),
            JpaTestEntityWithGeneratedId(name = "Original Entity 2", description = "Original Description 2", active = true)
        )
        val insertedEntities = jpaTestEntityWithGeneratedIdRepository.upsertAll(originalEntities)

        // Now create updated entities with the same IDs
        val updatedEntities = listOf(
            JpaTestEntityWithGeneratedId(
                id = insertedEntities[0].id,
                name = "Original entity 1",
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
        assertEquals(2, results.size)

        assertEquals(insertedEntities[0].id, results[0].id)
        assertEquals("Original Entity 1", results[0].name)
        assertEquals("Updated Description 1", results[0].description)
        assertEquals(false, results[0].active)

        assertEquals(insertedEntities[1].id, results[1].id)
        assertEquals("Original Entity 2", results[1].name)
        assertEquals("Original Description 2", results[1].description)
        assertEquals(true, results[1].active)

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
                name = "Original Entity",
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
        assertEquals(2, results.size)

        assertEquals(insertedEntity.id, results[0].id)
        assertEquals("Original Entity", results[0].name)
        assertEquals("Updated Description", results[0].description)
        assertEquals(false, results[0].active)

        assertNotNull(results[1].id)
        assertEquals("New Entity", results[1].name)
        assertEquals("New Description", results[1].description)
        assertEquals(true, results[1].active)
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
        assertNotNull(result.id)
        assertEquals("Entity With Null Description", result.name)
        assertEquals(null, result.description)
        assertEquals(true, result.active)
    }

    @Test
    fun `should handle empty list for upsertAll`() {
        // Given
        val emptyList = emptyList<JpaTestEntityWithGeneratedId>()

        // When
        val results = jpaTestEntityWithGeneratedIdRepository.upsertAll(emptyList)

        // Then
        assertEquals(0, results.size)
    }
}
