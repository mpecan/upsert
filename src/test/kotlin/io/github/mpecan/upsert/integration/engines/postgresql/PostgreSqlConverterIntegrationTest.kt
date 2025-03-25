package io.github.mpecan.upsert.integration.engines.postgresql

import io.github.mpecan.upsert.integration.TestContainerDefinitions
import io.github.mpecan.upsert.integration.base.AbstractConverterIntegrationTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container

/**
 * Integration tests for PostgreSQL UpsertRepository implementation with custom converters.
 */
class PostgreSqlConverterIntegrationTest : AbstractConverterIntegrationTest() {

    companion object {
        @Container
        @JvmStatic
        val postgresContainer = TestContainerDefinitions.createPostgresqlContainer()

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            TestContainerDefinitions.setupCommonProperties(registry, postgresContainer)
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
        }
    }

    override fun setupTables() {
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS test_json_entity (
                id SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL UNIQUE,
                attributes JSONB,
                tags JSONB,
                metadata JSONB,
                created_at TIMESTAMP
            )
        """
        )
    }
}