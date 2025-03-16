package si.pecan.upsert.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import si.pecan.upsert.dialect.PostgreSqlUpsertDialect
import si.pecan.upsert.entity.JpaTestEntityWithConverter
import si.pecan.upsert.entity.JsonData
import si.pecan.upsert.entity.JsonDataConverter
import si.pecan.upsert.repository.JdbcUpsertOperations
import javax.sql.DataSource

/**
 * Integration tests for upsert operations with custom converters.
 */
@Testcontainers
class ConverterIntegrationTest {

    @Container
    private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:13")
        .apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

    private lateinit var dataSource: DataSource
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var upsertOperations: JdbcUpsertOperations

    @BeforeEach
    fun setup() {
        // Create a DataSource connected to the PostgreSQL container
        dataSource = org.springframework.jdbc.datasource.DriverManagerDataSource().apply {
            url = postgresContainer.jdbcUrl
            username = postgresContainer.username
            password = postgresContainer.password
        }

        // Create a JdbcTemplate
        jdbcTemplate = JdbcTemplate(dataSource)

        // Create the UpsertOperations
        val dialect = PostgreSqlUpsertDialect()
        upsertOperations = JdbcUpsertOperations(jdbcTemplate, dialect)

        // Create the test tables
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS jpa_test_entity_with_converter (
                id BIGINT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                json_data TEXT,
                active BOOLEAN NOT NULL
            )
        """)
    }

    @Test
    fun `test upsert with custom converter`() {
        // Create a test entity with JSON data
        val jsonData = JsonData("test-key", "test-value")
        val entity = JpaTestEntityWithConverter(1, "Test Entity", jsonData, true)

        // Debug: Print the entity
        println("[DEBUG_LOG] Entity: $entity")

        // Debug: Print the converter output
        val converter = JsonDataConverter()
        val convertedValue = converter.convertToDatabaseColumn(jsonData)
        println("[DEBUG_LOG] Converted JSON: $convertedValue")

        try {
            // Perform the upsert operation
            val rowsAffected = upsertOperations.upsert(entity, "jpa_test_entity_with_converter")

            // Verify that one row was affected
            assertEquals(1, rowsAffected)

            // Verify that the entity was inserted correctly
            val result = jdbcTemplate.queryForMap("SELECT * FROM jpa_test_entity_with_converter WHERE id = ?", 1)
            println("[DEBUG_LOG] Query result: $result")
            assertEquals("Test Entity", result["name"])
            assertEquals("""{"key":"test-key","value":"test-value"}""", result["json_data"])
            assertEquals(true, result["active"])
        } catch (e: Exception) {
            println("[DEBUG_LOG] Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun `test upsert update with custom converter`() {
        try {
            // First insert an entity
            val jsonData1 = JsonData("original-key", "original-value")
            val entity1 = JpaTestEntityWithConverter(2, "Original Entity", jsonData1, true)
            println("[DEBUG_LOG] First entity: $entity1")

            // Debug: Print the converter output
            val converter = JsonDataConverter()
            val convertedValue1 = converter.convertToDatabaseColumn(jsonData1)
            println("[DEBUG_LOG] First converted JSON: $convertedValue1")

            val firstResult = upsertOperations.upsert(entity1, "jpa_test_entity_with_converter")
            println("[DEBUG_LOG] First upsert result: $firstResult")

            // Then update it with a new entity
            val jsonData2 = JsonData("updated-key", "updated-value")
            val entity2 = JpaTestEntityWithConverter(2, "Updated Entity", jsonData2, false)
            println("[DEBUG_LOG] Second entity: $entity2")

            val convertedValue2 = converter.convertToDatabaseColumn(jsonData2)
            println("[DEBUG_LOG] Second converted JSON: $convertedValue2")

            val rowsAffected = upsertOperations.upsert(entity2, "jpa_test_entity_with_converter")
            println("[DEBUG_LOG] Second upsert result: $rowsAffected")

            // Verify that one row was affected
            assertEquals(1, rowsAffected)

            // Verify that the entity was updated correctly
            val result = jdbcTemplate.queryForMap("SELECT * FROM jpa_test_entity_with_converter WHERE id = ?", 2)
            println("[DEBUG_LOG] Query result: $result")
            assertEquals("Updated Entity", result["name"])
            assertEquals("""{"key":"updated-key","value":"updated-value"}""", result["json_data"])
            assertEquals(false, result["active"])
        } catch (e: Exception) {
            println("[DEBUG_LOG] Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
