package si.pecan.upsert.processor

import si.pecan.upsert.dialect.ColumnInfo
import si.pecan.upsert.dialect.UpsertDialect
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Id

/**
 * Processor for handling JPA entities and generating the appropriate SQL for upsert operations.
 */
class UpsertProcessor(private val dialect: UpsertDialect) {
    // Cache for key columns by entity class
    private val keyColumnsCache = mutableMapOf<Class<*>, List<ColumnInfo>>()

    // Cache for value columns by entity class
    private val valueColumnsCache = mutableMapOf<Class<*>, List<ColumnInfo>>()

    // Cache for SQL queries by entity class, table name, and batch size
    private val sqlCache = mutableMapOf<Triple<Class<*>, String, Int>, String>()

    /**
     * Process an entity class for upsert operations.
     *
     * @param entityClass The entity class
     * @param tableName The table name
     * @return The generated SQL query
     */
    fun processUpsertEntity(entityClass: Class<*>, tableName: String): String {
        return processBatchUpsertEntity(entityClass, tableName, 1)
    }

    /**
     * Process an entity class for batch upsert operations.
     * Uses a cache to avoid repeated SQL generation.
     *
     * @param entityClass The entity class
     * @param tableName The table name
     * @param batchSize The number of entities in the batch
     * @return The generated SQL query
     */
    fun processBatchUpsertEntity(entityClass: Class<*>, tableName: String, batchSize: Int): String {
        // Check the cache first
        val cacheKey = Triple(entityClass, tableName, batchSize)
        return sqlCache.getOrPut(cacheKey) {
            // Get the key and value columns from the entity class (these are also cached)
            val keyColumns = getKeyColumns(entityClass)
            val valueColumns = getValueColumns(entityClass)

            // Check if we have at least one key column and one value column
            if (keyColumns.isEmpty()) {
                throw IllegalArgumentException("No key fields found in ${entityClass.name}. Use @Id or @EmbeddedId annotations to mark key fields.")
            }
            if (valueColumns.isEmpty()) {
                throw IllegalArgumentException("No value fields found in ${entityClass.name}. Ensure there are non-key fields in the entity.")
            }

            // Generate the SQL using the regular batch method
            dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, batchSize)
        }
    }

    /**
     * Check if the dialect supports optimized batch operations.
     *
     * @return True if the dialect supports optimized batch operations, false otherwise
     */
    fun supportsOptimizedBatch(): Boolean {
        return dialect.supportsOptimizedBatch()
    }

    /**
     * Get the column names for fields annotated with @Id or @EmbeddedId.
     * Uses a cache to avoid repeated reflection.
     *
     * @param entityClass The entity class
     * @return The list of column names
     */
    private fun getKeyColumns(entityClass: Class<*>): List<ColumnInfo> {
        // Check the cache first
        return keyColumnsCache.getOrPut(entityClass) {
            entityClass.declaredFields
                .filter {
                    it.isAnnotationPresent(Id::class.java) ||
                            it.isAnnotationPresent(EmbeddedId::class.java)
                }
                .map { field ->
                    // Check if the field has a @Column annotation
                    val columnAnnotation = field.getAnnotation(Column::class.java)
                    val name = if (columnAnnotation != null && columnAnnotation.name.isNotBlank()) {
                        // Use the column name from the annotation
                        columnAnnotation.name
                    } else {
                        // Use the field name as the column name
                        field.name
                    }
                    ColumnInfo(name, field.name)
                }
        }
    }

    /**
     * Get the column names for all non-key fields.
     * All fields that are not annotated with @Id or @EmbeddedId are considered value columns.
     * Uses a cache to avoid repeated reflection.
     *
     * @param entityClass The entity class
     * @return The list of column names
     */
    private fun getValueColumns(entityClass: Class<*>): List<ColumnInfo> {
        // Check the cache first
        return valueColumnsCache.getOrPut(entityClass) {
            // Get the key fields
            val keyFields = entityClass.declaredFields
                .filter {
                    it.isAnnotationPresent(Id::class.java) ||
                            it.isAnnotationPresent(EmbeddedId::class.java)
                }

            // Get all non-key fields
            entityClass.declaredFields
                .filter { field -> !keyFields.contains(field) }
                .map { field ->
                    // Check if the field has a @Column annotation
                    val columnAnnotation = field.getAnnotation(Column::class.java)
                    val name = if (columnAnnotation != null && columnAnnotation.name.isNotBlank()) {
                        // Use the column name from the annotation
                        columnAnnotation.name
                    } else {
                        // Use the field name as the column name
                        field.name
                    }
                    ColumnInfo(name, field.name)
                }
        }
    }
}
