package io.github.mpecan.upsert.integration.engines.postgresql

import io.github.mpecan.upsert.integration.base.AbstractConditionalUpsertIntegrationTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

/**
 * PostgreSQL-specific integration tests for conditional upsert operations.
 */
class PostgreSqlConditionalUpsertIntegrationTest : AbstractConditionalUpsertIntegrationTest() {

    companion object {
        @Container
        @JvmStatic
        val postgresContainer = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create" }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
        }
    }
}