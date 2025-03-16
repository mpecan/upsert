package si.pecan.upsert.integration

import org.junit.jupiter.api.Assertions.assertEquals
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
import si.pecan.upsert.entity.JpaTestEntity


/**
 * Integration tests for MySQL UpsertRepository implementation.
 */
@SpringBootTest(classes = [TestApplication::class])
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MySqlRepositoryIntegrationTest {

    private val logger = LoggerFactory.getLogger(MySqlRepositoryIntegrationTest::class.java)

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
    private lateinit var jpaTestEntityRepository: JpaTestEntityRepository

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
        val entity = JpaTestEntity(3, "JPA Test Entity", "JPA Test Description", true)

        // When
        val result = jpaTestEntityRepository.upsert(entity)

        // Then
        assertEquals(3L, result.id)
        assertEquals("JPA Test Entity", result.name)
        assertEquals("JPA Test Description", result.description)
        assertEquals(true, result.active)
    }

    @Test
    fun `should update existing jpa entity using repository`() {
        // Given
        val entity1 = JpaTestEntity(4, "Original JPA Entity", "Original JPA Description", true)
        val entity2 = JpaTestEntity(4, "Updated JPA Entity", "Updated JPA Description", false)

        // Insert the original entity
        jpaTestEntityRepository.upsert(entity1)

        // When
        val result = jpaTestEntityRepository.upsert(entity2)

        // Then

        assertEquals(4L, result.id)
        assertEquals("Updated JPA Entity", result.name)
        assertEquals("Updated JPA Description", result.description)
        assertEquals(false, result.active)
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
        val results = jpaTestEntityRepository.upsertAll(entities)

        // Then
        assertEquals(3, results.size)

        assertEquals(5L, results[0].id)
        assertEquals("JPA Entity 1", results[0].name)
        assertEquals("Description 1", results[0].description)
        assertEquals(true, results[0].active)

        assertEquals(6L, results[1].id)
        assertEquals("JPA Entity 2", results[1].name)
        assertEquals("Description 2", results[1].description)
        assertEquals(false, results[1].active)

        assertEquals(7L, results[2].id)
        assertEquals("JPA Entity 3", results[2].name)
        assertEquals("Description 3", results[2].description)
        assertEquals(true, results[2].active)
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
        val results = jpaTestEntityRepository.upsertAll(updatedEntities)

        // Then
        assertEquals(2, results.size)

        assertEquals(8L, results[0].id)
        assertEquals("Updated Entity 1", results[0].name)
        assertEquals("Updated Description 1", results[0].description)
        assertEquals(false, results[0].active)

        assertEquals(9L, results[1].id)
        assertEquals("Updated Entity 2", results[1].name)
        assertEquals("Updated Description 2", results[1].description)
        assertEquals(false, results[1].active)
    }

    @Test
    fun `should handle mix of new and existing jpa entities using repository`() {
        // Given
        // Insert an entity that will be updated
        jpaTestEntityRepository.upsert(JpaTestEntity(10, "Original Entity", "Original Description", true))

        val mixedEntities = listOf(
            JpaTestEntity(10, "Updated Entity", "Updated Description", false), // Existing entity to update
            JpaTestEntity(11, "New Entity", "New Description", true)          // New entity to insert
        )

        // When
        val results = jpaTestEntityRepository.upsertAll(mixedEntities)

        // Then
        assertEquals(2, results.size)

        assertEquals(10L, results[0].id)
        assertEquals("Updated Entity", results[0].name)
        assertEquals("Updated Description", results[0].description)
        assertEquals(false, results[0].active)

        assertEquals(11L, results[1].id)
        assertEquals("New Entity", results[1].name)
        assertEquals("New Description", results[1].description)
        assertEquals(true, results[1].active)
    }

    @Test
    fun `should handle jpa entity with null description`() {
        // Given
        val entity = JpaTestEntity(12, "Entity With Null Description", null, true)

        // When
        val result = jpaTestEntityRepository.upsert(entity)

        // Then
        assertEquals(12L, result.id)
        assertEquals("Entity With Null Description", result.name)
        assertEquals(null, result.description)
        assertEquals(true, result.active)
    }

    @Test
    fun `should handle empty list for upsertAll`() {
        // Given
        val emptyList = emptyList<JpaTestEntity>()

        // When
        val results = jpaTestEntityRepository.upsertAll(emptyList)

        // Then
        assertEquals(0, results.size)
    }
}
