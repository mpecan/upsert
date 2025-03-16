package si.pecan.upsert.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import si.pecan.upsert.dialect.MySqlUpsertDialect
import si.pecan.upsert.dialect.PostgreSqlUpsertDialect
import si.pecan.upsert.dialect.UpsertDialect
import si.pecan.upsert.dialect.UpsertDialectFactory
import si.pecan.upsert.repository.JdbcUpsertOperations
import si.pecan.upsert.repository.UpsertOperations
import javax.sql.DataSource

/**
 * Configuration class for upsert functionality.
 */
@Configuration
class UpsertConfiguration {

    /**
     * Create a UpsertDialectFactory bean.
     *
     * @param dataSource The data source
     * @return The UpsertDialectFactory
     */
    @Bean
    fun upsertDialectFactory(dataSource: DataSource): UpsertDialectFactory {
        return UpsertDialectFactory(dataSource)
    }

    /**
     * Create a UpsertDialect bean.
     *
     * @param dialectFactory The dialect factory
     * @return The UpsertDialect
     */
    @Bean
    fun upsertDialect(dialectFactory: UpsertDialectFactory): UpsertDialect {
        return dialectFactory.getDialect()
    }

    /**
     * Create a UpsertOperations bean.
     *
     * @param jdbcTemplate The JDBC template
     * @param dialect The upsert dialect
     * @return The UpsertOperations
     */
    @Bean
    fun upsertOperations(jdbcTemplate: JdbcTemplate, dialect: UpsertDialect): UpsertOperations {
        return JdbcUpsertOperations(jdbcTemplate, dialect)
    }
}