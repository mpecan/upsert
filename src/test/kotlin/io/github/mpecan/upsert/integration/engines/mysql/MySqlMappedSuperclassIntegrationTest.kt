package io.github.mpecan.upsert.integration.engines.mysql

import io.github.mpecan.upsert.integration.TestContainerDefinitions
import io.github.mpecan.upsert.integration.base.AbstractMappedSuperclassIntegrationTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container

/**
 * MySQL-specific integration tests for @MappedSuperclass support.
 * Tests that entities with inherited fields from @MappedSuperclass work correctly
 * with MySQL database and upsert operations.
 */
class MySqlMappedSuperclassIntegrationTest : AbstractMappedSuperclassIntegrationTest() {
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
}