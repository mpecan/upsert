package si.pecan.upsert.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import si.pecan.upsert.dialect.UpsertDialect
import si.pecan.upsert.dialect.UpsertDialectFactory
import javax.sql.DataSource

/**
 * Auto-configuration for upsert functionality.
 * This class automatically configures the upsert functionality based on the libraries provided on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass(JdbcTemplate::class)
class UpsertAutoConfiguration {

    /**
     * Create a UpsertDialectFactory bean if it doesn't exist.
     *
     * @param dataSource The data source
     * @return The UpsertDialectFactory
     */
    @Bean
    @ConditionalOnMissingBean
    fun upsertDialectFactory(dataSource: DataSource): UpsertDialectFactory {
        return UpsertDialectFactory(dataSource)
    }

    /**
     * Create a UpsertDialect bean if it doesn't exist.
     *
     * @param dialectFactory The dialect factory
     * @return The UpsertDialect
     */
    @Bean
    @ConditionalOnMissingBean
    fun upsertDialect(dialectFactory: UpsertDialectFactory): UpsertDialect {
        return dialectFactory.getDialect()
    }
}
