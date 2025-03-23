package io.github.mpecan.upsert.integration

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Test application for PostgreSQL repository integration tests.
 */
@SpringBootApplication
@EntityScan("io.github.mpecan.upsert.entity")
@EnableJpaRepositories(
    basePackages = ["io.github.mpecan.upsert.integration"],
)
class TestApplication