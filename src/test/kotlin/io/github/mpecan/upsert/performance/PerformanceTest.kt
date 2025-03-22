package io.github.mpecan.upsert.performance

import io.github.mpecan.upsert.entity.JpaTestEntity
import io.github.mpecan.upsert.integration.JpaTestEntityRepository
import io.github.mpecan.upsert.integration.TestApplication
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.system.measureTimeMillis

@SpringBootTest(classes = [TestApplication::class])
@Testcontainers
class PerformanceTest {

    private val logger = LoggerFactory.getLogger(PerformanceTest::class.java)

    companion object {
        @Container
        @JvmStatic
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
    fun testSaveAllPerformance() {
        // Given
        val entities = (1..10000).map {
            JpaTestEntity(it.toLong(), "Entity $it", "Description $it", true)
        }

        // When
        val timeTaken = measureTimeMillis {
            jpaTestEntityRepository.saveAll(entities)
        }

        // Then
        logger.info("Time taken for saveAll: $timeTaken ms")
        assertTrue(timeTaken < 60000, "saveAll took too long")
    }

    @Test
    fun testUpsertAllPerformance() {
        // Given
        val entities = (1..10000).map {
            JpaTestEntity(it.toLong(), "Entity $it", "Description $it", true)
        }

        // When
        val timeTaken = measureTimeMillis {
            jpaTestEntityRepository.upsertAll(entities)
        }

        // Then
        logger.info("Time taken for upsertAll: $timeTaken ms")
        assertTrue(timeTaken < 60000, "upsertAll took too long")
    }
}
