package si.pecan.upsert.repository

import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import si.pecan.upsert.dialect.UpsertDialect
import si.pecan.upsert.processor.UpsertProcessor
import java.lang.reflect.Field
import java.sql.Connection
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Id

/**
 * Implementation of UpsertOperations that uses JDBC to execute upsert operations.
 */
class JdbcUpsertOperations(
    private val jdbcTemplate: JdbcTemplate,
    private val dialect: UpsertDialect
) : UpsertOperations {

    private val processor = UpsertProcessor(dialect)

    /**
     * Perform an upsert operation for the given entity.
     *
     * @param entity The entity to upsert
     * @param tableName The table name
     * @param <T> The entity type
     * @return The number of rows affected
     */
    override fun <T : Any> upsert(entity: T, tableName: String): Int {
       return upsertAll(listOf(entity), tableName)
    }

    /**
     * Perform an upsert operation for the given list of entities.
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

        // Check if the dialect supports optimized batch operations
        return if (processor.supportsOptimizedBatch()) {
            // For optimized batch operations (e.g., PostgreSQL unnest),
            // we'll use the batchUpdate method with ExtendedBeanPropertySqlParameterSource

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
            results.sum()
        } else {
            // Generate the SQL using the processor for batch operations
            val sql = processor.processBatchUpsertEntity(entityClass, tableName, entities.size)

            // For regular batch operations, extract parameter values from all entities
            // This approach works for both MySQL and PostgreSQL
            val allParamValues = entities.flatMap { entity -> 
                extractParameterValues(entity)
            }

            // Execute the SQL with all parameters
            jdbcTemplate.update(sql, *allParamValues.toTypedArray())
        }
    }

    /**
     * Determine the SQL type for a value array.
     * Maps Java/Kotlin types to PostgreSQL types.
     *
     * @param values The array of values
     * @return The SQL type name
     */
    private fun getSqlType(values: Array<Any?>): String {
        // Find the first non-null value to determine the type
        val nonNullValue = values.firstOrNull { it != null }

        return when (nonNullValue) {
            is Int -> "integer"
            is Long -> "bigint"
            is Float, is Double -> "float8"
            is Boolean -> "boolean"
            is String -> "text"
            else -> "text" // Default to text for other types
        }
    }

    /**
     * Get the key fields from the entity class.
     *
     * @param entityClass The entity class
     * @return The list of key fields
     */
    private fun getKeyFields(entityClass: Class<*>): List<Field> {
        return entityClass.declaredFields
            .filter { 
                it.isAnnotationPresent(Id::class.java) ||
                it.isAnnotationPresent(EmbeddedId::class.java)
            }
    }

    /**
     * Get the value fields from the entity class.
     *
     * @param entityClass The entity class
     * @return The list of value fields
     */
    private fun getValueFields(entityClass: Class<*>): List<Field> {
        // Use all non-key fields
        val keyFieldNames = getKeyFields(entityClass).map { it.name }
        return entityClass.declaredFields.filter { !keyFieldNames.contains(it.name) }
    }


    /**
     * Extract parameter values from the entity.
     *
     * @param entity The entity
     * @return The list of parameter values
     */
    private fun <T : Any> extractParameterValues(entity: T): List<Any?> {
        val entityClass = entity.javaClass

        // Get key fields (using JPA annotations)
        val keyFields = getKeyFields(entityClass)

        // Get value fields (all non-key fields)
        val valueFields = getValueFields(entityClass)

        // Combine key and value fields
        val allFields = keyFields + valueFields

        // Extract values from fields
        return allFields.map { field ->
            field.isAccessible = true
            field.get(entity)
        }
    }
}
