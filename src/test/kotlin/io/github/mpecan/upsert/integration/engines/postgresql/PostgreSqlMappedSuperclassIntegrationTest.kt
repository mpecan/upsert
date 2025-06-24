package io.github.mpecan.upsert.integration.engines.postgresql

import io.github.mpecan.upsert.integration.TestContainerDefinitions
import io.github.mpecan.upsert.integration.base.AbstractMappedSuperclassIntegrationTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container

/**
 * PostgreSQL-specific integration tests for @MappedSuperclass support.
 * Tests that entities with inherited fields from @MappedSuperclass work correctly
 * with PostgreSQL database and upsert operations.
 */
class PostgreSqlMappedSuperclassIntegrationTest : AbstractMappedSuperclassIntegrationTest() {
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
}