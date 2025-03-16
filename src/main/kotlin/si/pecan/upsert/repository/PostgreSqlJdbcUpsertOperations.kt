package si.pecan.upsert.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import si.pecan.upsert.dialect.PostgreSqlUpsertDialect
import si.pecan.upsert.dialect.UpsertDialect

/**
 * PostgreSQL-specific implementation of UpsertOperations.
 * Uses the optimized batch operations with named parameters.
 */
class PostgreSqlJdbcUpsertOperations(
    jdbcTemplate: JdbcTemplate,
    dialect: UpsertDialect
) : AbstractJdbcUpsertOperations(jdbcTemplate, dialect) {

    /**
     * Perform an upsert operation for the given list of entities.
     * Uses PostgreSQL's optimized batch operations with named parameters.
     *
     * @param entities The list of entities to upsert
     * @param tableName The table name
     * @param <T> The entity type
     * @return The total number of rows affected
     */
    override fun <T : Any> upsertAll(entities: List<T>, tableName: String): Int {
        if (entities.isEmpty()) {
            return 0
        }

        val entityClass = entities.first().javaClass

        // Generate the SQL for a single entity
        val sql = processor.processBatchUpsertEntity(entityClass, tableName, 1)

        // Create parameter sources for each entity
        val paramSources = entities.map { entity -> 
            ExtendedBeanPropertySqlParameterSource(entity)
        }.toTypedArray()

        // Create a NamedParameterJdbcTemplate
        val namedJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        // Execute batch update and sum the results
        val results = namedJdbcTemplate.batchUpdate(sql, paramSources)
        return results.sum()
    }

    companion object {
        /**
         * Factory method to create a PostgreSqlJdbcUpsertOperations instance.
         *
         * @param jdbcTemplate The JdbcTemplate to use
         * @return A new PostgreSqlJdbcUpsertOperations instance
         */
        fun create(jdbcTemplate: JdbcTemplate): PostgreSqlJdbcUpsertOperations {
            return PostgreSqlJdbcUpsertOperations(jdbcTemplate, PostgreSqlUpsertDialect())
        }
    }
}