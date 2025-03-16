package si.pecan.upsert.processor

import si.pecan.upsert.dialect.ColumnInfo
import si.pecan.upsert.dialect.UpsertDialect
import javax.persistence.Column
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
        return processBatchUpsertEntity(entityClass, tableName, 1)
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

        // Generate the SQL using the regular batch method
        return dialect.generateBatchUpsertSql(tableName, keyColumns, valueColumns, batchSize)
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
    private fun getKeyColumns(entityClass: Class<*>): List<ColumnInfo> {
        return entityClass.declaredFields
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

    /**
     * Get the column names for fields annotated with @UpsertValue.
     * If no fields are annotated with @UpsertValue, all non-key fields are considered value columns.
     *
     * @param entityClass The entity class
     * @return The list of column names
     */
    private fun getValueColumns(entityClass: Class<*>): List<ColumnInfo> {
        // Get the key fields
        val keyFields = entityClass.declaredFields
            .filter {
                it.isAnnotationPresent(Id::class.java) ||
                        it.isAnnotationPresent(EmbeddedId::class.java)
            }

        // Get all non-key fields
        return entityClass.declaredFields
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
