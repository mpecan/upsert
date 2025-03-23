package io.github.mpecan.upsert.performance.reporting

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Utility class for generating performance test reports in markdown format.
 */
object PerformanceReportGenerator {

    /**
     * Generate a markdown report from the test results and save it to a file.
     *
     * @param results The test results to include in the report
     * @param outputPath The path where the report should be saved
     * @return The path to the generated report file
     */
    fun generateReport(
        results: List<PerformanceTestResult>,
        outputPath: String = "PERFORMANCE-REPORT.md"
    ): String {
        val reportBuilder = StringBuilder()

        // Add report header
        val timestamp =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        reportBuilder.appendLine("# Performance Test Report")
        reportBuilder.appendLine("Generated on: $timestamp")
        reportBuilder.appendLine()

        // Add summary
        reportBuilder.appendLine("## Summary")
        reportBuilder.appendLine()
        reportBuilder.appendLine("This report compares the performance of two approaches for inserting/updating entities:")
        reportBuilder.appendLine("1. `upsertAll` - Uses a custom implementation that generates SQL UPSERT statements")
        reportBuilder.appendLine("2. `saveAll` - Uses the standard Spring Data JPA implementation")
        reportBuilder.appendLine()
        reportBuilder.appendLine("Each test was run multiple times to get an average performance measurement, reducing the impact of external factors.")
        reportBuilder.appendLine()

        // Group results by test type
        val groupedResults = results.groupBy {
            if (it.batchSize != null) "Batch Size Performance [total of ${it.entityCount}]" else it.testName
        }

        // Process each test type
        groupedResults.forEach { (testType, testResults) ->
            reportBuilder.appendLine("## $testType")
            reportBuilder.appendLine()

            if (testType.startsWith("Batch Size Performance")) {
                generateBatchSizeReport(reportBuilder, testResults)
            } else {
                generateStandardReport(reportBuilder, testResults)
            }

            reportBuilder.appendLine()
        }


        // Write the report to a file
        val reportFile = File(outputPath)
        reportFile.writeText(reportBuilder.toString())

        return outputPath
    }

    private fun formatTimeDuration(time: Long): String {
        return if (abs(time) < 1000) {
            "$time ns"
        } else if (abs(time) < 1000000) {
            "${String.format("%.2f", time.toDouble() / 1000)} µs"
        } else if (abs(time) < 1000000000) {
            "${String.format("%.2f", time.toDouble() / 1000000)} ms"
        } else {
            "${String.format("%.2f", time.toDouble() / 1000000000)} s"
        }
    }

    /**
     * Generate a report section for standard performance tests.
     */
    private fun generateStandardReport(
        reportBuilder: StringBuilder,
        results: List<PerformanceTestResult>
    ) {
        // Create a table with columns for database type, entity count, avg upsert time, avg saveAll time, difference, and ratio
        reportBuilder.appendLine("| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |")
        reportBuilder.appendLine("|----------|--------------|----------------------|----------------------|-----------------|----------|")

        // Add a row for each test result
        results.sortedBy { it.entityCount }.forEach { result ->
            reportBuilder.appendLine(
                "| ${result.databaseType} | ${result.entityCount} | ${
                    formatTimeDuration(
                        result.avgUpsertTime
                    )
                } | ${formatTimeDuration(result.avgSaveAllTime)} | ${formatTimeDuration(result.avgDifference)} | ${
                    String.format(
                        "%.2f",
                        result.avgRatio * 100
                    )
                }% |"
            )
        }
    }

    /**
     * Generate a report section for batch size performance tests.
     */
    private fun generateBatchSizeReport(
        reportBuilder: StringBuilder,
        results: List<PerformanceTestResult>
    ) {
        // Group results by database type
        val groupedByDb = results.groupBy { it.databaseType }

        groupedByDb.forEach { (dbType, dbResults) ->
            reportBuilder.appendLine("### $dbType")
            reportBuilder.appendLine()

            // Create a table with columns for batch size, avg upsert time, avg saveAll time, difference, and ratio
            reportBuilder.appendLine("| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |")
            reportBuilder.appendLine("|------------|----------------------|----------------------|-----------------|----------|")

            // Add a row for each batch size
            dbResults.sortedBy { it.batchSize }.forEach { result ->
                reportBuilder.appendLine(
                    "| ${result.batchSize} | ${formatTimeDuration(result.avgUpsertTime)} | ${
                        formatTimeDuration(
                            result.avgSaveAllTime
                        )
                    } | ${formatTimeDuration(result.avgDifference)} | ${
                        String.format(
                            "%.1f",
                            result.avgRatio * 100
                        )
                    }% |"
                )
            }

            reportBuilder.appendLine()
        }
    }
}