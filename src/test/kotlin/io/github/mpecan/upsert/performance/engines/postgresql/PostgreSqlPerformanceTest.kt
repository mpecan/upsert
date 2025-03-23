package io.github.mpecan.upsert.performance.engines.postgresql

import io.github.mpecan.upsert.performance.AbstractPerformanceTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container

/**
 * Performance tests for PostgreSQL comparing upsert operations with Spring Data JPA saveAll operations.
 * Each test is run multiple times to get an average performance measurement.
 */
class PostgreSqlPerformanceTest : AbstractPerformanceTest() {

    override val databaseType: String = "PostgreSQL"

    companion object {
        @Container
        @JvmStatic
        val mysqlContainer = PostgreSQLContainer<Nothing>("postgres:13").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysqlContainer.jdbcUrl }
            registry.add("spring.datasource.username") { mysqlContainer.username }
            registry.add("spring.datasource.password") { mysqlContainer.password }
            registry.add("spring.datasource.driver-class-name") { mysqlContainer.driverClassName }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }
}