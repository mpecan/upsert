package si.pecan.upsert.repository

import org.springframework.jdbc.core.JdbcTemplate
import si.pecan.upsert.dialect.MySqlUpsertDialect
import si.pecan.upsert.dialect.UpsertDialect

/**
 * MySQL-specific implementation of UpsertOperations.
 * Uses regular batch operations with positional parameters.
 */
class MySqlJdbcUpsertOperations(
    jdbcTemplate: JdbcTemplate,
    dialect: UpsertDialect
) : AbstractJdbcUpsertOperations(jdbcTemplate, dialect) {

    /**
     * Perform an upsert operation for the given list of entities.
     * Uses MySQL's regular batch operations with positional parameters.
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

        // Generate the SQL using the processor for batch operations
        val sql = processor.processBatchUpsertEntity(entityClass, tableName, entities.size)

        // For regular batch operations, extract parameter values from all entities
        val allParamValues = entities.flatMap { entity -> 
            extractParameterValues(entity)
        }

        // Execute the SQL with all parameters
        return jdbcTemplate.update(sql, *allParamValues.toTypedArray())
    }

    companion object {
        /**
         * Factory method to create a MySqlJdbcUpsertOperations instance.
         *
         * @param jdbcTemplate The JdbcTemplate to use
         * @return A new MySqlJdbcUpsertOperations instance
         */
        fun create(jdbcTemplate: JdbcTemplate): MySqlJdbcUpsertOperations {
            return MySqlJdbcUpsertOperations(jdbcTemplate, MySqlUpsertDialect())
        }
    }
}