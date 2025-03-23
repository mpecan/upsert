package io.github.mpecan.upsert.performance.reporting

import org.slf4j.Logger
import kotlin.system.measureNanoTime

/**
 * Data class to represent the result of a performance test.
 */
data class PerformanceTestResult(
    val testName: String,
    val databaseType: String,
    val entityCount: Int,
    val batchSize: Int? = null,
    val upsertTimes: List<Long>,
    val saveAllTimes: List<Long>
) {
    val avgUpsertTime: Long
        get() = upsertTimes.average().toLong()

    val avgSaveAllTime: Long
        get() = saveAllTimes.average().toLong()

    val avgDifference: Long
        get() = avgUpsertTime - avgSaveAllTime

    val avgRatio: Double
        get() = avgUpsertTime.toDouble() / avgSaveAllTime.toDouble()
}

/**
 * Utility object for performance testing.
 */
object PerformanceTestUtils {
    private val results = mutableListOf<PerformanceTestResult>()

    /**
     * Run a performance test multiple times and calculate average performance.
     *
     * @param testName The name of the test
     * @param databaseType The type of database (MySQL, PostgreSQL)
     * @param entityCount The number of entities used in the test
     * @param batchSize The batch size (if applicable)
     * @param repetitions The number of times to repeat the test
     * @param logger The logger to use for logging results
     * @param setupFn Function to set up the test
     * @param upsertFn Function to perform the upsert operation
     * @param saveAllFn Function to perform the saveAll operation
     * @param cleanupFn Function to clean up after each test iteration
     * @return The test result
     */
    fun runPerformanceTest(
        testName: String,
        databaseType: String,
        entityCount: Int,
        batchSize: Int? = null,
        repetitions: Int = 10,
        logger: Logger,
        setupFn: () -> Unit,
        upsertFn: () -> Unit,
        saveAllFn: () -> Unit,
        cleanupFn: () -> Unit
    ): PerformanceTestResult {
        val upsertTimes = mutableListOf<Long>()
        val saveAllTimes = mutableListOf<Long>()

        repeat(repetitions) { iteration ->
            // Setup for upsert test
            setupFn()

            // Measure time for upsert
            val upsertTime = measureNanoTime {
                upsertFn()
            }
            upsertTimes.add(upsertTime)

            // Cleanup
            cleanupFn()

            // Setup for saveAll test
            setupFn()

            // Measure time for saveAll
            val saveAllTime = measureNanoTime {
                saveAllFn()
            }
            saveAllTimes.add(saveAllTime)

            // Cleanup
            cleanupFn()

            // Log the results for this iteration
            logger.info("$testName - Iteration ${iteration + 1}/$repetitions:")
            logger.info("  Upsert time: $upsertTime ns")
            logger.info("  SaveAll time: $saveAllTime ns")
            logger.info("  Difference: ${upsertTime - saveAllTime} ns")
            logger.info("  Ratio: ${upsertTime.toDouble() / saveAllTime.toDouble()}")
        }

        // Create and store the test result
        val result = PerformanceTestResult(
            testName = testName,
            databaseType = databaseType,
            entityCount = entityCount,
            batchSize = batchSize,
            upsertTimes = upsertTimes,
            saveAllTimes = saveAllTimes
        )
        results.add(result)

        // Log the average results
        logger.info("$testName - Average results over $repetitions iterations:")
        logger.info("  Avg Upsert time: ${result.avgUpsertTime} ns")
        logger.info("  Avg SaveAll time: ${result.avgSaveAllTime} ns")
        logger.info("  Avg Difference: ${result.avgDifference} ns")
        logger.info("  Avg Ratio: ${result.avgRatio}")

        return result
    }

    /**
     * Get all test results.
     */
    fun getAllResults(): List<PerformanceTestResult> = results.toList()
}