package si.pecan.upsert.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import si.pecan.upsert.entity.JpaTestEntity
import si.pecan.upsert.repository.JdbcUpsertOperations
import javax.sql.DataSource

/**
 * Integration tests for MySQL upsert operations.
 */
@Testcontainers
class MySqlIntegrationTest {

    companion object {
        @Container
        val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }
    }

    private lateinit var dataSource: DataSource
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var upsertOperations: JdbcUpsertOperations

    @BeforeEach
    fun setUp() {
        // Set up the data source
        dataSource = DriverManagerDataSource().apply {
            url = mysqlContainer.jdbcUrl
            username = mysqlContainer.username
            password = mysqlContainer.password
            setDriverClassName(mysqlContainer.driverClassName)
        }

        // Set up the JDBC template
        jdbcTemplate = JdbcTemplate(dataSource)

        // Set up the upsert operations
        upsertOperations = JdbcUpsertOperations.forMySql(jdbcTemplate)

        // Create the test tables
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS jpa_test_entity (
                id BIGINT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                description VARCHAR(255),
                active BOOLEAN NOT NULL
            )
        """)
    }

    @Test
    fun `should insert new entity`() {
        // Given
        val entity = JpaTestEntity(1, "Test Entity", "Test Description", true)

        // When
        val result = upsertOperations.upsert(entity, "jpa_test_entity")

        assertEquals(1L, result.id)
        assertEquals("Test Entity", result.name)
        assertEquals("Test Description", result.description)
        assertEquals(true, result.active)
    }

    @Test
    fun `should update existing entity`() {
        // Given
        val entity1 = JpaTestEntity(2, "Original Entity", "Original Description", true)
        val entity2 = JpaTestEntity(2, "Updated Entity", "Updated Description", false)

        // Insert the original entity
        upsertOperations.upsert(entity1, "jpa_test_entity")

        // When
        val result = upsertOperations.upsert(entity2, "jpa_test_entity")

        assertEquals(2L, result.id)
        assertEquals("Updated Entity", result.name)
        assertEquals("Updated Description", result.description)
        assertEquals(false, result.active)
    }

    @Test
    fun `should insert new jpa entity`() {
        // Given
        val entity = JpaTestEntity(3, "JPA Test Entity", "JPA Test Description", true)

        // When
        val result = upsertOperations.upsert(entity, "jpa_test_entity")

        assertEquals(3L, result.id)
        assertEquals("JPA Test Entity", result.name)
        assertEquals("JPA Test Description", result.description)
        assertEquals(true, result.active)
    }

    @Test
    fun `should update existing jpa entity`() {
        // Given
        val entity1 = JpaTestEntity(4, "Original JPA Entity", "Original JPA Description", true)
        val entity2 = JpaTestEntity(4, "Updated JPA Entity", "Updated JPA Description", false)

        // Insert the original entity
        upsertOperations.upsert(entity1, "jpa_test_entity")

        // When
        val result = upsertOperations.upsert(entity2, "jpa_test_entity")


        // Verify the entity was updated

        assertEquals(4L, result.id)
        assertEquals("Updated JPA Entity", result.name)
        assertEquals("Updated JPA Description", result.description)
        assertEquals(false, result.active)
    }

    @Test
    fun `should upsert multiple entities`() {
        // Given
        val entities = listOf(
            JpaTestEntity(5, "Entity 5", "Description 5", true),
            JpaTestEntity(6, "Entity 6", "Description 6", false),
            JpaTestEntity(7, "Entity 7", "Description 7", true)
        )

        // When
        val results = upsertOperations.upsertAll(entities, "jpa_test_entity")

        assertEquals(5L, results[0].id)
        assertEquals("Entity 5", results[0].name)
        assertEquals("Description 5", results[0].description)
        assertEquals(true, results[0].active)

        assertEquals(6L, results[1].id)
        assertEquals("Entity 6", results[1].name)
        assertEquals("Description 6", results[1].description)
        assertEquals(false, results[1].active)

        assertEquals(7L, results[2].id)
        assertEquals("Entity 7", results[2].name)
        assertEquals("Description 7", results[2].description)
        assertEquals(true, results[2].active)
    }

    @Test
    fun `should update multiple existing entities`() {
        // Given
        val originalEntities = listOf(
            JpaTestEntity(8, "Original Entity 8", "Original Description 8", true),
            JpaTestEntity(9, "Original Entity 9", "Original Description 9", true)
        )

        val updatedEntities = listOf(
            JpaTestEntity(8, "Updated Entity 8", "Updated Description 8", false),
            JpaTestEntity(9, "Updated Entity 9", "Updated Description 9", false)
        )

        // Insert the original entities
        upsertOperations.upsertAll(originalEntities, "jpa_test_entity")

        // When
        val results = upsertOperations.upsertAll(updatedEntities, "jpa_test_entity")

        // Then
        assertEquals(2, results.size)

        assertEquals(8L, results[0].id)
        assertEquals("Updated Entity 8", results[0].name)
        assertEquals("Updated Description 8", results[0].description)
        assertEquals(false, results[0].active)

        assertEquals(9L, results[1].id)
        assertEquals("Updated Entity 9", results[1].name)
        assertEquals("Updated Description 9", results[1].description)
        assertEquals(false, results[1].active)
    }
}
