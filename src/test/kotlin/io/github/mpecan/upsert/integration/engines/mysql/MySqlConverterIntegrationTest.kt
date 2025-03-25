package io.github.mpecan.upsert.integration.engines.mysql

import io.github.mpecan.upsert.integration.TestContainerDefinitions
import io.github.mpecan.upsert.integration.base.AbstractConverterIntegrationTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container

/**
 * Integration tests for MySQL UpsertRepository implementation with custom converters.
 */
class MySqlConverterIntegrationTest : AbstractConverterIntegrationTest() {

    companion object {
        @Container
        @JvmStatic
        val mysqlContainer = TestContainerDefinitions.createMySqlContainer()

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            TestContainerDefinitions.setupCommonProperties(registry, mysqlContainer)
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.MySQL8Dialect" }
        }
    }

    override fun setupTables() {
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS test_json_entity (
                id SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL UNIQUE ,
                attributes JSON,
                tags JSON,
                metadata JSON,
                created_at TIMESTAMP
            )
        """
        )
    }
}