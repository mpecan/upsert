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

    // Cache for key fields by entity class
    private val keyFieldsCache = mutableMapOf<Class<*>, List<Field>>()

    // Cache for value fields by entity class
    private val valueFieldsCache = mutableMapOf<Class<*>, List<Field>>()

    // Store entity class, ID class, and table name
    protected var entityClass: Class<*>? = null
    protected var idClass: Class<*>? = null
    protected var tableName: String? = null

    /**
     * Initialize the operations with entity class and ID class.
     * This method should be called once at startup to prepare the operations.
     *
     * @param entityClass The entity class
     * @param idClass The ID class
     * @param tableName The table name
     */
    override fun initialize(entityClass: Class<*>, idClass: Class<*>, tableName: String) {
        this.entityClass = entityClass
        this.idClass = idClass
        this.tableName = tableName

        // Pre-cache key and value fields
        getKeyFields(entityClass)
        getValueFields(entityClass)
    }

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
     * Perform an upsert operation for the given entity with custom ON clause and ignored fields.
     *
     * @param entity The entity to upsert
     * @param tableName The table name
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @param <T> The entity type
     * @return The number of rows affected
     */
    override fun <T : Any> upsert(
        entity: T,
        tableName: String,
        onFields: List<String>,
        ignoredFields: List<String>,
        ignoreAllFields: Boolean
    ): Int {
        return upsertAll(listOf(entity), tableName, onFields, ignoredFields, ignoreAllFields)
    }

    /**
     * Get the key fields from the entity class.
     * Uses a cache to avoid repeated reflection.
     *
     * @param entityClass The entity class
     * @return The list of key fields
     */
    protected fun getKeyFields(entityClass: Class<*>): List<Field> {
        // Check the cache first
        return keyFieldsCache.getOrPut(entityClass) {
            entityClass.declaredFields
                .filter { 
                    it.isAnnotationPresent(Id::class.java) ||
                    it.isAnnotationPresent(EmbeddedId::class.java)
                }
        }
    }

    /**
     * Get the key fields from the entity class based on field names.
     *
     * @param entityClass The entity class
     * @param fieldNames The field names to use as keys
     * @return The list of key fields
     */
    protected fun getKeyFieldsByName(entityClass: Class<*>, fieldNames: List<String>): List<Field> {
        // Convert field names to column names
        val columnNames = fieldNames.map { it.lowercase() }

        // Get all fields from the entity class
        return entityClass.declaredFields.filter { field ->
            // Get the column name for the field
            val columnName = getColumnName(field).lowercase()
            // Check if the column name is in the list of column names
            columnNames.contains(columnName)
        }
    }

    /**
     * Get the value fields from the entity class.
     * Uses a cache to avoid repeated reflection.
     *
     * @param entityClass The entity class
     * @return The list of value fields
     */
    protected fun getValueFields(entityClass: Class<*>): List<Field> {
        // Check the cache first
        return valueFieldsCache.getOrPut(entityClass) {
            // Use all non-key fields
            val keyFieldNames = getKeyFields(entityClass).map { it.name }
            entityClass.declaredFields.filter { !keyFieldNames.contains(it.name) }
        }
    }

    /**
     * Get the column name for a field.
     *
     * @param field The field
     * @return The column name
     */
    protected fun getColumnName(field: Field): String {
        // Check if the field has a @Column annotation
        val columnAnnotation = field.getAnnotation(Column::class.java)
        return if (columnAnnotation != null && columnAnnotation.name.isNotBlank()) {
            // Use the column name from the annotation
            columnAnnotation.name
        } else {
            // Use the field name as the column name
            field.name
        }
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
