package io.github.mpecan.upsert.performance

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Test application for performance tests.
 */
@SpringBootApplication
@EntityScan(basePackages = ["io.github.mpecan.upsert.performance", "io.github.mpecan.upsert.entity"])
@EnableJpaRepositories(
    basePackages = ["io.github.mpecan.upsert.performance"],
)
class PerformanceTestApplication