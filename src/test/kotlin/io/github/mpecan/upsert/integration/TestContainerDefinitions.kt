package io.github.mpecan.upsert.integration

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer

object TestContainerDefinitions {

    fun createMySqlContainer() = MySQLContainer<Nothing>("mysql:8.0").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
    }

    fun createPostgresqlContainer() = PostgreSQLContainer<Nothing>("postgres:14-alpine").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
    }

    fun setupCommonProperties(
        registry: DynamicPropertyRegistry,
        container: JdbcDatabaseContainer<*>
    ) {
        registry.add("spring.datasource.url") { container.jdbcUrl }
        registry.add("spring.datasource.username") { container.username }
        registry.add("spring.datasource.password") { container.password }
        registry.add("spring.datasource.driver-class-name") { container.driverClassName }
        registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
    }
}