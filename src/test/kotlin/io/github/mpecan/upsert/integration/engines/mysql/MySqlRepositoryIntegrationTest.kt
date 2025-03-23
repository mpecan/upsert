package io.github.mpecan.upsert.integration.engines.mysql

import io.github.mpecan.upsert.integration.TestContainerDefinitions
import io.github.mpecan.upsert.integration.base.AbstractRepositoryIntegrationTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container

/**
 * Integration tests for MySQL UpsertRepository implementation.
 */
class MySqlRepositoryIntegrationTest : AbstractRepositoryIntegrationTest() {
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