package si.pecan.upsert.repository

import org.springframework.jdbc.core.JdbcTemplate
import si.pecan.upsert.dialect.UpsertDialect
import si.pecan.upsert.processor.UpsertProcessor
import java.lang.reflect.Field
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Id

/**
 * Abstract base class for JDBC-based upsert operations.
 * Provides common functionality for different database-specific implementations.
 */
abstract class AbstractJdbcUpsertOperations(
    protected val jdbcTemplate: JdbcTemplate,
    protected val dialect: UpsertDialect
) : UpsertOperations {

    protected val processor = UpsertProcessor(dialect)

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
     * Get the key fields from the entity class.
     *
     * @param entityClass The entity class
     * @return The list of key fields
     */
    protected fun getKeyFields(entityClass: Class<*>): List<Field> {
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
    protected fun getValueFields(entityClass: Class<*>): List<Field> {
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
    protected fun <T : Any> extractParameterValues(entity: T): List<Any?> {
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