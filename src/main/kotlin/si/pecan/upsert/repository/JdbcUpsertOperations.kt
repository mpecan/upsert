package si.pecan.upsert.repository

import org.springframework.jdbc.core.JdbcTemplate
import si.pecan.upsert.dialect.MySqlUpsertDialect
import si.pecan.upsert.dialect.PostgreSqlUpsertDialect
import si.pecan.upsert.dialect.UpsertDialect

/**
 * Factory class for creating database-specific UpsertOperations implementations.
 * This class provides backward compatibility with existing code while allowing
 * for engine-specific optimizations.
 */
class JdbcUpsertOperations(
    private val jdbcTemplate: JdbcTemplate,
    private val dialect: UpsertDialect
) : UpsertOperations {

    // Delegate to the appropriate implementation based on the dialect
    private val delegate: UpsertOperations = when (dialect) {
        is PostgreSqlUpsertDialect -> PostgreSqlJdbcUpsertOperations(jdbcTemplate, dialect)
        is MySqlUpsertDialect -> MySqlJdbcUpsertOperations(jdbcTemplate, dialect)
        else -> {
            // For other dialects, use PostgreSQL if it supports optimized batch, otherwise MySQL
            if (dialect.supportsOptimizedBatch()) {
                PostgreSqlJdbcUpsertOperations(jdbcTemplate, dialect)
            } else {
                MySqlJdbcUpsertOperations(jdbcTemplate, dialect)
            }
        }
    }

    /**
     * Perform an upsert operation for the given entity.
     * Delegates to the appropriate implementation based on the dialect.
     *
     * @param entity The entity to upsert
     * @param tableName The table name
     * @param <T> The entity type
     * @return The number of rows affected
     */
    override fun <T : Any> upsert(entity: T, tableName: String): Int {
        return delegate.upsert(entity, tableName)
    }

    /**
     * Perform an upsert operation for the given list of entities.
     * Delegates to the appropriate implementation based on the dialect.
     *
     * @param entities The list of entities to upsert
     * @param tableName The table name
     * @param <T> The entity type
     * @return The total number of rows affected
     */
    override fun <T : Any> upsertAll(entities: List<T>, tableName: String): Int {
        return delegate.upsertAll(entities, tableName)
    }

    companion object {
        /**
         * Create a JdbcUpsertOperations instance for PostgreSQL.
         *
         * @param jdbcTemplate The JdbcTemplate to use
         * @return A new JdbcUpsertOperations instance for PostgreSQL
         */
        fun forPostgreSql(jdbcTemplate: JdbcTemplate): JdbcUpsertOperations {
            return JdbcUpsertOperations(jdbcTemplate, PostgreSqlUpsertDialect())
        }

        /**
         * Create a JdbcUpsertOperations instance for MySQL.
         *
         * @param jdbcTemplate The JdbcTemplate to use
         * @return A new JdbcUpsertOperations instance for MySQL
         */
        fun forMySql(jdbcTemplate: JdbcTemplate): JdbcUpsertOperations {
            return JdbcUpsertOperations(jdbcTemplate, MySqlUpsertDialect())
        }
    }
}
