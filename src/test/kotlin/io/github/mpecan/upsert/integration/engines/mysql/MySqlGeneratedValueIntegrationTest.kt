package io.github.mpecan.upsert.integration.engines.mysql

import io.github.mpecan.upsert.integration.TestContainerDefinitions
import io.github.mpecan.upsert.integration.base.AbstractSqlGeneratedValueIntegrationTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container

/**
 * Integration tests for entities with @GeneratedValue annotations.
 * These tests verify that the upsert operations work correctly with entities that have
 * auto-generated IDs (the field should be nullable and a var).
 */
class MySqlGeneratedValueIntegrationTest : AbstractSqlGeneratedValueIntegrationTest() {


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

    override fun createTable() {
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS jpa_test_entity_with_generated_id (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(255) NOT NULL UNIQUE,
                description VARCHAR(255),
                active BOOLEAN NOT NULL
            )
        """.trimIndent()
        )
    }
}