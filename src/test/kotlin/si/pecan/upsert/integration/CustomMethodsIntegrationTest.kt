package si.pecan.upsert.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.jdbc.BadSqlGrammarException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import si.pecan.upsert.entity.JpaTestEntity

/**
 * Integration tests for custom upsert methods.
 * This class tests the method name parsing functionality.
 */
@SpringBootTest(classes = [TestApplication::class])
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomMethodsIntegrationTest {

    private val logger = LoggerFactory.getLogger(CustomMethodsIntegrationTest::class.java)

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:14-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgresContainer.username }
            registry.add("spring.datasource.password") { postgresContainer.password }
            registry.add("spring.datasource.driver-class-name") { postgresContainer.driverClassName }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }

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
    fun `should upsert successfully using id as ON clause`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(1, "Test Entity", "Updated Description", false)

        // Insert the first entity
        customMethodsTestRepository.upsertOnId(entity1)

        // When
        val result = customMethodsTestRepository.upsertOnId(entity2)

        // Then
        assertEquals(1, result)
    }

    @Test
    fun `should upsert successfully ignoring name as IGNORING clause`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Updated Description", false)

        // Insert the first entity
        customMethodsTestRepository.upsert(entity1)

        // When
        val result = customMethodsTestRepository.upsertIgnoringName(entity2)

        // Then
        assertEquals(1, result)
    }

    @Test
    fun `should throw exception when using name as ON clause without uniqueness constraint`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Updated Description", false)

        // Insert the first entity
        customMethodsTestRepository.upsert(entity1)

        // When/Then - expect exception when using name as ON clause
        val exception = assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertOnName(entity2)
        }

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
        assert(exception.message?.contains("name") == true)
    }

    @Test
    fun `should throw exception when using name as ON clause and ignoring active field without uniqueness constraint`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Updated Description", false)

        // Insert the first entity
        customMethodsTestRepository.upsert(entity1)

        // When/Then - expect exception when using name as ON clause
        val exception = assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertOnNameIgnoringActive(entity2)
        }

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
        assert(exception.message?.contains("name") == true)
    }

    @Test
    fun `should throw exception when using name and description as ON clause without uniqueness constraint`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Description A", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Description B", false)

        // Insert the first entity
        customMethodsTestRepository.upsert(entity1)

        // When/Then - expect exception when using name and description as ON clause
        val exception = assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertOnNameAndDescription(entity2)
        }

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
        assert(exception.message?.contains("name, description") == true)
    }

    @Test
    fun `should throw exception when using name as ON clause and ignoring all fields without uniqueness constraint`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Updated Description", false)

        // Insert the first entity
        customMethodsTestRepository.upsert(entity1)

        // When/Then - expect exception when using name as ON clause and ignoring all fields
        val exception = assertThrows(BadSqlGrammarException::class.java) {
            customMethodsTestRepository.upsertOnNameIgnoringAllFields(entity2)
        }

        // Verify the exception message
        assert(exception.message?.contains("syntax error") == true || 
               exception.message?.contains("SQL grammar") == true)
    }

    @Test
    fun `should throw exception when using name as ON clause for multiple entities without uniqueness constraint`() {
        // Given
        val entities1 = listOf(
            JpaTestEntity(1, "Entity A", "Description 1", true),
            JpaTestEntity(2, "Entity B", "Description 2", true)
        )

        val entities2 = listOf(
            JpaTestEntity(3, "Entity A", "Updated Description 1", false),
            JpaTestEntity(4, "Entity B", "Updated Description 2", false),
            JpaTestEntity(5, "Entity C", "Description 3", true)
        )

        // Insert the first set of entities
        customMethodsTestRepository.upsertAll(entities1)

        // When/Then - expect exception when using name as ON clause
        val exception = assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertAllOnName(entities2)
        }

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
        assert(exception.message?.contains("name") == true)
    }

    @Test
    fun `should throw exception when using name as ON clause for multiple entities and ignoring active field without uniqueness constraint`() {
        // Given
        val entities1 = listOf(
            JpaTestEntity(1, "Entity A", "Description 1", true),
            JpaTestEntity(2, "Entity B", "Description 2", true)
        )

        val entities2 = listOf(
            JpaTestEntity(3, "Entity A", "Updated Description 1", false),
            JpaTestEntity(4, "Entity B", "Updated Description 2", false)
        )

        // Insert the first set of entities
        customMethodsTestRepository.upsertAll(entities1)

        // When/Then - expect exception when using name as ON clause
        val exception = assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertAllOnNameIgnoringActive(entities2)
        }

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
        assert(exception.message?.contains("name") == true)
    }
}
