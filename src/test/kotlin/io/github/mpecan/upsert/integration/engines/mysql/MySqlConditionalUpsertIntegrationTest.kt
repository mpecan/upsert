package io.github.mpecan.upsert.integration.engines.mysql

import io.github.mpecan.upsert.integration.base.AbstractConditionalUpsertIntegrationTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

/**
 * MySQL integration tests for conditional upsert operations on MySQL 8.0.19+.
 * These tests use the modern alias syntax which avoids MySQL VALUES() function limitations.
 * 
 * The modern implementation generates SQL like:
 * INSERT ... VALUES ... AS new_values ON DUPLICATE KEY UPDATE 
 * col = IF(new_values.condition > condition, new_values.col, col)
 */
class MySqlConditionalUpsertIntegrationTest : AbstractConditionalUpsertIntegrationTest() {

    companion object {
        @Container
        @JvmStatic
        val mysqlContainer = MySQLContainer(DockerImageName.parse("mysql:8.0.41"))
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl)
            registry.add("spring.datasource.username", mysqlContainer::getUsername)
            registry.add("spring.datasource.password", mysqlContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create" }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.MySQLDialect" }
        }
    }
}