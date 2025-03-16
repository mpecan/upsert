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

    // Store the pre-generated SQL query
    private var preGeneratedSql: String? = null

    /**
     * Initialize the operations with entity class and ID class.
     * For PostgreSQL, we can generate the entire SQL query at startup.
     *
     * @param entityClass The entity class
     * @param idClass The ID class
     * @param tableName The table name
     */
    override fun initialize(entityClass: Class<*>, idClass: Class<*>, tableName: String) {
        super.initialize(entityClass, idClass, tableName)

        // Generate the SQL for a single entity
        preGeneratedSql = processor.processBatchUpsertEntity(entityClass, tableName, 1)
    }

    /**
     * Perform an upsert operation for the given list of entities.
     * Uses PostgreSQL's optimized batch operations with named parameters.
     * Uses a pre-generated SQL query for better performance.
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

        // Use the pre-generated SQL if available, otherwise generate it
        val sql = if (preGeneratedSql != null && this.tableName == tableName) {
            preGeneratedSql!!
        } else {
            val entityClass = entities.first().javaClass
            processor.processBatchUpsertEntity(entityClass, tableName, 1)
        }

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
