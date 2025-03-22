package io.github.mpecan.upsert.performance

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

/**
 * Performance tests for MySQL comparing upsert operations with Spring Data JPA saveAll operations.
 */
@SpringBootTest(classes = [PerformanceTestApplication::class])
@Testcontainers
class PostgreSqlPerformanceTest {

    private val logger = LoggerFactory.getLogger(PostgreSqlPerformanceTest::class.java)

    companion object {
        @Container
        @JvmStatic
        val mysqlContainer = PostgreSQLContainer<Nothing>("postgres:13").apply {
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
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }

    @Autowired
    private lateinit var performanceTestRepository: PerformanceTestRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        try {
            // Clear the tables before each test
            jdbcTemplate.execute("DELETE FROM performance_test_entity")
            logger.info("Tables cleared successfully")
        } catch (e: Exception) {
            logger.error("Error clearing tables", e)
            throw e
        }
    }

    @AfterEach
    fun tearDown() {
        try {
            // Clear the tables after each test
            jdbcTemplate.execute("DELETE FROM performance_test_entity")
            logger.info("Tables cleared successfully")
        } catch (e: Exception) {
            logger.error("Error clearing tables", e)
            throw e
        }
    }

    /**
     * Generate a list of test entities with the given size.
     */
    private fun generateEntities(size: Int, startId: Long = 1): List<PerformanceTestEntity> {
        return (startId until startId + size).map { id ->
            PerformanceTestEntity(
                id = id,
                name = "Entity $id",
                description = "Description for entity $id",
                active = id % 2 == 0L,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                counter = id.toInt(),
                amount = id.toDouble(),
                code = "CODE-$id",
                tags = "tag1,tag2,tag3"
            )
        }
    }

    /**
     * Test the performance of inserting new entities using upsert vs. saveAll.
     */
    @ParameterizedTest
    @ValueSource(ints = [1, 10, 100, 1000])
    fun `test insert performance - upsert vs saveAll`(size: Int) {
        // Generate test data
        val entities = generateEntities(size)

        // Measure time for upsert
        val upsertTime = measureTimeMillis {
            performanceTestRepository.upsertAll(entities)
        }

        // Clear the table
        jdbcTemplate.execute("DELETE FROM performance_test_entity")

        // Measure time for saveAll
        val saveAllTime = measureTimeMillis {
            performanceTestRepository.saveAll(entities)
        }

        // Log the results
        logger.info("Insert performance test with $size entities:")
        logger.info("  Upsert time: $upsertTime ms")
        logger.info("  SaveAll time: $saveAllTime ms")
        logger.info("  Difference: ${upsertTime - saveAllTime} ms")
        logger.info("  Ratio: ${upsertTime.toDouble() / saveAllTime.toDouble()}")
    }

    /**
     * Test the performance of updating existing entities using upsert vs. saveAll.
     */
    @ParameterizedTest
    @ValueSource(ints = [1, 10, 100, 1000])
    fun `test update performance - upsert vs saveAll`(size: Int) {
        // Generate and insert initial entities
        val initialEntities = generateEntities(size)
        performanceTestRepository.saveAll(initialEntities)

        // Generate updated entities with the same IDs
        val updatedEntities = generateEntities(size).map { entity ->
            entity.copy(
                name = "Updated ${entity.name}",
                description = "Updated ${entity.description}",
                updatedAt = LocalDateTime.now(),
                counter = entity.counter + 1,
                amount = entity.amount * 2,
                tags = "updated,tags"
            )
        }

        // Measure time for upsert
        val upsertTime = measureTimeMillis {
            performanceTestRepository.upsertAll(updatedEntities)
        }

        // Clear the table and reinsert initial entities
        jdbcTemplate.execute("DELETE FROM performance_test_entity")
        performanceTestRepository.saveAll(initialEntities)

        // Measure time for saveAll
        val saveAllTime = measureTimeMillis {
            performanceTestRepository.saveAll(updatedEntities)
        }

        // Log the results
        logger.info("Update performance test with $size entities:")
        logger.info("  Upsert time: $upsertTime ms")
        logger.info("  SaveAll time: $saveAllTime ms")
        logger.info("  Difference: ${upsertTime - saveAllTime} ms")
        logger.info("  Ratio: ${upsertTime.toDouble() / saveAllTime.toDouble()}")
    }

    /**
     * Test the performance of a mix of inserting and updating entities using upsert vs. saveAll.
     */
    @ParameterizedTest
    @ValueSource(ints = [10, 100, 1000])
    fun `test mixed insert-update performance - upsert vs saveAll`(size: Int) {
        // Generate and insert half of the entities
        val halfSize = size / 2
        val initialEntities = generateEntities(halfSize)
        performanceTestRepository.saveAll(initialEntities)

        // Generate a mix of new and updated entities
        val mixedEntities = generateEntities(halfSize) + generateEntities(halfSize, halfSize + 1L)

        // Measure time for upsert
        val upsertTime = measureTimeMillis {
            performanceTestRepository.upsertAll(mixedEntities)
        }

        // Clear the table and reinsert initial entities
        jdbcTemplate.execute("DELETE FROM performance_test_entity")
        performanceTestRepository.saveAll(initialEntities)

        // Measure time for saveAll
        val saveAllTime = measureTimeMillis {
            performanceTestRepository.saveAll(mixedEntities)
        }

        // Log the results
        logger.info("Mixed insert-update performance test with $size entities:")
        logger.info("  Upsert time: $upsertTime ms")
        logger.info("  SaveAll time: $saveAllTime ms")
        logger.info("  Difference: ${upsertTime - saveAllTime} ms")
        logger.info("  Ratio: ${upsertTime.toDouble() / saveAllTime.toDouble()}")
    }

    /**
     * Test the performance of batch operations with different batch sizes.
     */
    @Test
    fun `test batch size performance impact`() {
        val totalSize = 1000
        val entities = generateEntities(totalSize)

        // Test different batch sizes
        val batchSizes = listOf(1, 10, 50, 100, 200, 500, 1000)

        for (batchSize in batchSizes) {
            // Clear the table
            jdbcTemplate.execute("DELETE FROM performance_test_entity")

            // Measure time for upsert with the current batch size
            val upsertTime = measureTimeMillis {
                entities.chunked(batchSize).forEach { batch ->
                    performanceTestRepository.upsertAll(batch)
                }
            }

            // Clear the table
            jdbcTemplate.execute("DELETE FROM performance_test_entity")

            // Measure time for saveAll with the current batch size
            val saveAllTime = measureTimeMillis {
                entities.chunked(batchSize).forEach { batch ->
                    performanceTestRepository.saveAll(batch)
                }
            }

            // Log the results
            logger.info("Batch size performance test with batch size $batchSize:")
            logger.info("  Upsert time: $upsertTime ms")
            logger.info("  SaveAll time: $saveAllTime ms")
            logger.info("  Difference: ${upsertTime - saveAllTime} ms")
            logger.info("  Ratio: ${upsertTime.toDouble() / saveAllTime.toDouble()}")
        }
    }
}
