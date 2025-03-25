package io.github.mpecan.upsert.config

import io.github.mpecan.upsert.dialect.UpsertDialect
import io.github.mpecan.upsert.dialect.UpsertDialectFactory
import io.github.mpecan.upsert.repository.UpsertRepository
import io.github.mpecan.upsert.repository.UpsertRepositoryFactory
import io.github.mpecan.upsert.type.TypeMapperRegistry
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

/**
 * Auto-configuration for upsert functionality.
 * This class automatically configures the upsert functionality based on the libraries provided on the classpath.
 */
@AutoConfiguration
@AutoConfigureAfter(DataSourceAutoConfiguration::class, JpaRepositoriesAutoConfiguration::class)
@ConditionalOnClass(value = [JpaRepository::class, UpsertRepository::class])
@ConditionalOnBean(DataSource::class)
@Import(UpsertJpaRepositoriesRegistrar::class)
class UpsertAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun upsertDialectFactory(
        dataSource: DataSource,
        typeMapperRegistry: TypeMapperRegistry
    ): UpsertDialectFactory {
        return UpsertDialectFactory(dataSource, typeMapperRegistry)
    }

    @Bean
    @ConditionalOnMissingBean
    fun upsertDialect(dialectFactory: UpsertDialectFactory): UpsertDialect {
        return dialectFactory.getDialect()
    }

    @Bean
    @ConditionalOnMissingBean
    fun namedParameterJdbcTemplate(dataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = ["jpaRepositoryFactory"])
    fun jpaRepositoryFactory(
        entityManager: EntityManager,
        applicationContext: ApplicationContext,
        dataSource: DataSource,
        jdbcTemplate: NamedParameterJdbcTemplate,
        typeMapperRegistry: TypeMapperRegistry
    ): JpaRepositoryFactory {
        return UpsertRepositoryFactory(
            entityManager,
            applicationContext,
            dataSource,
            jdbcTemplate,
            typeMapperRegistry
        )
    }
}
