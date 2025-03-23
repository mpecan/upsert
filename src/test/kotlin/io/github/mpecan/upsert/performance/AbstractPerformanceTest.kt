package io.github.mpecan.upsert.performance

import io.github.mpecan.upsert.performance.entity.PerformanceTestEntity
import io.github.mpecan.upsert.performance.reporting.PerformanceTestUtils
import io.github.mpecan.upsert.performance.repository.PerformanceTestRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

/**
 * Base performance test class to be used when comparing upsert operations with Spring Data JPA saveAll operations.
 * Each test is run multiple times to get an average performance measurement.
 */
@SpringBootTest(classes = [PerformanceTestApplication::class])
@Testcontainers
@Tag("performance")
abstract class AbstractPerformanceTest {

    abstract val databaseType: String

    private val logger = LoggerFactory.getLogger(AbstractPerformanceTest::class.java)

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
    @ValueSource(ints = [10, 100, 1000])
    fun `test insert performance - upsert vs saveAll`(size: Int) {
        // Generate test data
        val entities = generateEntities(size)

        // Run the performance test
        PerformanceTestUtils.runPerformanceTest(
            testName = "Insert Performance",
            databaseType = databaseType,
            entityCount = size,
            repetitions = 10,
            logger = logger,
            setupFn = {
                // No setup needed for insert test
            },
            upsertFn = {
                performanceTestRepository.upsertAll(entities)
            },
            saveAllFn = {
                performanceTestRepository.saveAll(entities)
            },
            cleanupFn = {
                jdbcTemplate.execute("DELETE FROM performance_test_entity")
            }
        )
    }

    /**
     * Test the performance of updating existing entities using upsert vs. saveAll.
     */
    @ParameterizedTest
    @ValueSource(ints = [10, 100, 1000])
    fun `test update performance - upsert vs saveAll`(size: Int) {
        // Generate initial entities
        val initialEntities = generateEntities(size)

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

        // Run the performance test
        PerformanceTestUtils.runPerformanceTest(
            testName = "Update Performance",
            databaseType = databaseType,
            entityCount = size,
            repetitions = 10,
            logger = logger,
            setupFn = {
                // Insert initial entities
                performanceTestRepository.saveAll(initialEntities)
            },
            upsertFn = {
                performanceTestRepository.upsertAll(updatedEntities)
            },
            saveAllFn = {
                performanceTestRepository.saveAll(updatedEntities)
            },
            cleanupFn = {
                jdbcTemplate.execute("DELETE FROM performance_test_entity")
            }
        )
    }

    /**
     * Test the performance of a mix of inserting and updating entities using upsert vs. saveAll.
     */
    @ParameterizedTest
    @ValueSource(ints = [10, 100, 1000])
    fun `test mixed insert-update performance - upsert vs saveAll`(size: Int) {
        // Calculate half size
        val halfSize = size / 2

        // Generate initial entities (half of the total)
        val initialEntities = generateEntities(halfSize)

        // Generate a mix of new and updated entities
        val mixedEntities = generateEntities(halfSize) + generateEntities(halfSize, halfSize + 1L)

        // Run the performance test
        PerformanceTestUtils.runPerformanceTest(
            testName = "Mixed Insert-Update Performance",
            databaseType = databaseType,
            entityCount = size,
            repetitions = 10,
            logger = logger,
            setupFn = {
                // Insert initial entities (half of the total)
                performanceTestRepository.saveAll(initialEntities)
            },
            upsertFn = {
                performanceTestRepository.upsertAll(mixedEntities)
            },
            saveAllFn = {
                performanceTestRepository.saveAll(mixedEntities)
            },
            cleanupFn = {
                jdbcTemplate.execute("DELETE FROM performance_test_entity")
            }
        )
    }

    /**
     * Test the performance of batch operations with different batch sizes.
     */
    @Test
    fun `test batch size performance impact`() {
        val totalSize = 1000
        val entities = generateEntities(totalSize)

        // Test different batch sizes
        val batchSizes = listOf(10, 50, 100, 200, 500, 1000)

        for (batchSize in batchSizes) {
            // Run the performance test for this batch size
            PerformanceTestUtils.runPerformanceTest(
                testName = "Batch Size Performance",
                databaseType = databaseType,
                entityCount = totalSize,
                batchSize = batchSize,
                repetitions = 10,
                logger = logger,
                setupFn = {
                    // No setup needed
                },
                upsertFn = {
                    entities.chunked(batchSize).forEach { batch ->
                        performanceTestRepository.upsertAll(batch)
                    }
                },
                saveAllFn = {
                    entities.chunked(batchSize).forEach { batch ->
                        performanceTestRepository.saveAll(batch)
                    }
                },
                cleanupFn = {
                    jdbcTemplate.execute("DELETE FROM performance_test_entity")
                }
            )
        }
    }
}
