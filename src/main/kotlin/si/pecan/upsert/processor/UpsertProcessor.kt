package si.pecan.upsert.processor

import si.pecan.upsert.dialect.UpsertDialect
import si.pecan.upsert.model.UpsertModel
import jakarta.persistence.*

/**
 * Processor for handling JPA entities and generating the appropriate SQL for upsert operations.
 */
class UpsertProcessor(private val dialect: UpsertDialect) {
     // Cache for SQL queries by entity class, table name, and batch size
    private val sqlCache = mutableMapOf<Triple<Class<*>, String, Int>, String>()

    // Cache for custom SQL queries
    private val customSqlCache = mutableMapOf<CustomSqlCacheKey, String>()

    /**
     * Process an entity class for upsert operations.
     *
     * @param entityClass The entity class
     * @param tableName The table name
     * @return The generated SQL query
     */
    fun processUpsertEntity(upsertModel: UpsertModel): String {
        return processBatchUpsertEntity(upsertModel , 1)
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
    fun processBatchUpsertEntity(upsertModel: UpsertModel, batchSize: Int): String {
        // Check the cache first
        val cacheKey = Triple(upsertModel.entityClass, upsertModel.getTableName(), batchSize)
        return sqlCache.getOrPut(cacheKey) {
            val upsertInstance = upsertModel.createUpsertInstance()
            // Generate the SQL using the regular batch method
            dialect.generateBatchUpsertSql(
                upsertModel.getTableName(),
                upsertInstance.onColumns,
                upsertInstance.values,
                upsertInstance.updateColumns,
                batchSize
            )
        }
    }

    /**
     * Process an entity class for batch upsert operations with custom ON clause and ignored fields.
     * Uses a cache to avoid repeated SQL generation.
     *
     * @param entityClass The entity class
     * @param tableName The table name
     * @param batchSize The number of entities in the batch
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @return The generated SQL query
     */
    fun processBatchUpsertEntityCustom(
        upsertModel: UpsertModel,
        batchSize: Int,
        onFields: List<String>,
        ignoredFields: List<String>,
        ignoreAllFields: Boolean
    ): String {
        // Check the cache first
        val cacheKey = CustomSqlCacheKey(
            upsertModel.entityClass,
            upsertModel.getTableName(),
            batchSize,
            onFields,
            ignoredFields,
            ignoreAllFields
        )
        return customSqlCache.getOrPut(cacheKey) {
            val upsertInstance = upsertModel.createUpsertInstance(
                onFields,
                ignoreColumns = ignoredFields
            )
            // Generate the SQL using the regular batch method
            dialect.generateBatchUpsertSql(
                upsertModel.getTableName(),
                upsertInstance.onColumns,
                upsertInstance.values,
                upsertInstance.updateColumns,
                batchSize
            )
        }
    }
}
