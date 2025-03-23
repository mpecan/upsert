package io.github.mpecan.upsert.performance.reporting

import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan


class PerformanceTestListener : TestExecutionListener {
    override fun testPlanExecutionFinished(testPlan: TestPlan?) {
        super.testPlanExecutionFinished(testPlan)
        val results = PerformanceTestUtils.getAllResults()
        if (results.isNotEmpty()) {
            PerformanceReportGenerator.generateReport(results)
        }
    }
}