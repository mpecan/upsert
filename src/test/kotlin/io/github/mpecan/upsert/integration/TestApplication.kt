package io.github.mpecan.upsert.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Test application for PostgreSQL repository integration tests.
 */
@SpringBootApplication
@EntityScan("io.github.mpecan.upsert.entity", "io.github.mpecan.upsert.type.json")
@EnableJpaRepositories(
    basePackages = ["io.github.mpecan.upsert.integration"],
)
class TestApplication {

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
}