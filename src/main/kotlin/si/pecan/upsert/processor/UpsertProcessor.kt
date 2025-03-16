package si.pecan.upsert.processor

import si.pecan.upsert.dialect.UpsertDialect
import javax.persistence.EmbeddedId
import javax.persistence.Id

/**
 * Processor for handling @Upsert annotations and generating the appropriate SQL.
 */
class UpsertProcessor(private val dialect: UpsertDialect) {

    /**
     * Process an entity class for upsert operations.
     *
     * @param entityClass The entity class
     * @param tableName The table name
     * @return The generated SQL query
     */
    fun processUpsertEntity(entityClass: Class<*>, tableName: String): String {
        // Get the key and value columns from the entity class
        val keyColumns = getKeyColumns(entityClass)
        val valueColumns = getValueColumns(entityClass)

        // Check if we have at least one key column and one value column
        if (keyColumns.isEmpty()) {
            throw IllegalArgumentException("No key fields found in ${entityClass.name}. Use @UpsertKey, @Id, or @EmbeddedId annotations to mark key fields.")
        }
        if (valueColumns.isEmpty()) {
            throw IllegalArgumentException("No value fields found in ${entityClass.name}. Either use @UpsertValue annotations or ensure there are non-key fields in the entity.")
        }

        // Generate the SQL using the dialect
        return dialect.generateUpsertSql(tableName, keyColumns, valueColumns)
    }

    /**
     * Process an entity class for batch upsert operations.
     *
     * @param entityClass The entity class
     * @param tableName The table name
     * @param batchSize The number of entities in the batch
     * @return The generated SQL query
     */
    fun processBatchUpsertEntity(entityClass: Class<*>, tableName: String, batchSize: Int): String {
        // Get the key and value columns from the entity class
        val keyColumns = getKeyColumns(entityClass)
        val valueColumns = getValueColumns(entityClass)

        // Check if we have at least one key column and one value column
        if (keyColumns.isEmpty()) {
            throw IllegalArgumentException("No key fields found in ${entityClass.name}. Use @UpsertKey, @Id, or @EmbeddedId annotations to mark key fields.")
        }
        if (valueColumns.isEmpty()) {
            throw IllegalArgumentException("No value fields found in ${entityClass.name}. Either use @UpsertValue annotations or ensure there are non-key fields in the entity.")
        }

        // Check if the dialect supports optimized batch operations
        return if (dialect.supportsOptimizedBatch()) {
            // Generate the SQL using the optimized batch method
            dialect.generateOptimizedBatchUpsertSql(tableName, keyColumns, valueColumns, batchSize)
        } else {
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
     * Get the column names for fields annotated with @UpsertKey, @Id, or @EmbeddedId.
     *
     * @param entityClass The entity class
     * @return The list of column names
     */
    private fun getKeyColumns(entityClass: Class<*>): List<String> {
        return entityClass.declaredFields
            .filter {
                it.isAnnotationPresent(Id::class.java) ||
                it.isAnnotationPresent(EmbeddedId::class.java)
            }
            .map { it.name }
    }

    /**
     * Get the column names for fields annotated with @UpsertValue.
     * If no fields are annotated with @UpsertValue, all non-key fields are considered value columns.
     *
     * @param entityClass The entity class
     * @return The list of column names
     */
    private fun getValueColumns(entityClass: Class<*>): List<String> {
        // Otherwise, use all non-key fields
        val keyColumns = getKeyColumns(entityClass)
        return entityClass.declaredFields
            .filter { !keyColumns.contains(it.name) }
            .map { it.name }
    }
}
