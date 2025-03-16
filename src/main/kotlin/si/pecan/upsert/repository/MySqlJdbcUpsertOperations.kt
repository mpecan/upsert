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

    // Store the pre-generated base query
    private var baseQueryTemplate: String? = null

    /**
     * Initialize the operations with entity class and ID class.
     * For MySQL, we can pre-generate the base query template, but the VALUES part
     * depends on the number of entities in the batch.
     *
     * @param entityClass The entity class
     * @param idClass The ID class
     * @param tableName The table name
     */
    override fun initialize(entityClass: Class<*>, idClass: Class<*>, tableName: String) {
        super.initialize(entityClass, idClass, tableName)

        // Generate a query for a single entity to use as a template
        val singleEntityQuery = processor.processBatchUpsertEntity(entityClass, tableName, 1)

        // Extract the base query template (everything except the VALUES part)
        // The template will have a placeholder for the VALUES part
        baseQueryTemplate = extractBaseQueryTemplate(singleEntityQuery)
    }

    /**
     * Extract the base query template from a single-entity query.
     * This removes the VALUES part and replaces it with a placeholder.
     *
     * @param singleEntityQuery The query for a single entity
     * @return The base query template
     */
    private fun extractBaseQueryTemplate(singleEntityQuery: String): String {
        // Find the VALUES part
        val valuesIndex = singleEntityQuery.indexOf(" VALUES ")
        if (valuesIndex == -1) {
            return singleEntityQuery
        }

        // Extract the part before VALUES
        val beforeValues = singleEntityQuery.substring(0, valuesIndex + " VALUES ".length)

        // Find the ON DUPLICATE KEY UPDATE part
        val onDuplicateIndex = singleEntityQuery.indexOf(" ON DUPLICATE KEY UPDATE ")
        if (onDuplicateIndex == -1) {
            return beforeValues + "%VALUES_PLACEHOLDER%"
        }

        // Extract the part after the first set of values
        val afterValues = singleEntityQuery.substring(onDuplicateIndex)

        // Combine the parts with a placeholder for the VALUES part
        return beforeValues + "%VALUES_PLACEHOLDER%" + afterValues
    }

    /**
     * Perform an upsert operation for the given list of entities.
     * Uses MySQL's regular batch operations with positional parameters.
     * Uses a pre-generated base query template for better performance.
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

        // Use the pre-generated base query template if available and for the same table
        val sql = if (baseQueryTemplate != null && this.tableName == tableName) {
            // Generate the VALUES part for the current batch size
            val singleEntityQuery = processor.processBatchUpsertEntity(entityClass, tableName, 1)
            val batchQuery = processor.processBatchUpsertEntity(entityClass, tableName, entities.size)

            // Extract the VALUES part from the batch query
            val valuesPart = extractValuesPart(singleEntityQuery, batchQuery)

            // Replace the placeholder with the actual VALUES part
            baseQueryTemplate!!.replace("%VALUES_PLACEHOLDER%", valuesPart)
        } else {
            // Fall back to generating the full query
            processor.processBatchUpsertEntity(entityClass, tableName, entities.size)
        }

        // For regular batch operations, extract parameter values from all entities
        val allParamValues = entities.flatMap { entity -> 
            extractParameterValues(entity)
        }

        // Execute the SQL with all parameters
        return jdbcTemplate.update(sql, *allParamValues.toTypedArray())
    }

    /**
     * Extract the VALUES part from a batch query by comparing it with a single-entity query.
     *
     * @param singleEntityQuery The query for a single entity
     * @param batchQuery The query for the batch
     * @return The VALUES part of the batch query
     */
    private fun extractValuesPart(singleEntityQuery: String, batchQuery: String): String {
        // Find the VALUES part in both queries
        val singleValuesIndex = singleEntityQuery.indexOf(" VALUES ")
        val batchValuesIndex = batchQuery.indexOf(" VALUES ")

        if (singleValuesIndex == -1 || batchValuesIndex == -1) {
            return ""
        }

        // Find the ON DUPLICATE KEY UPDATE part in both queries
        val singleOnDuplicateIndex = singleEntityQuery.indexOf(" ON DUPLICATE KEY UPDATE ")
        val batchOnDuplicateIndex = batchQuery.indexOf(" ON DUPLICATE KEY UPDATE ")

        if (singleOnDuplicateIndex == -1 || batchOnDuplicateIndex == -1) {
            return ""
        }

        // Extract the VALUES part from the batch query
        return batchQuery.substring(batchValuesIndex + " VALUES ".length, batchOnDuplicateIndex)
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
