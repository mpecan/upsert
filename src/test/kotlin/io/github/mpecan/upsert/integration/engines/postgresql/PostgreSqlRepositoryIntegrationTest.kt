package io.github.mpecan.upsert.integration.engines.postgresql

import io.github.mpecan.upsert.integration.TestContainerDefinitions
import io.github.mpecan.upsert.integration.base.AbstractRepositoryIntegrationTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container

/**
 * Integration tests for PostgreSQL UpsertRepository implementation.
 */
class PostgreSqlRepositoryIntegrationTest : AbstractRepositoryIntegrationTest() {

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