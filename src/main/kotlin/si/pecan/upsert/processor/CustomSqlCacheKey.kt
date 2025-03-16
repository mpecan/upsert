package si.pecan.upsert.processor

/**
 * Cache key for custom SQL queries.
 * Used to cache SQL queries by entity class, table name, batch size, on fields, and ignored fields.
 *
 * @param entityClass The entity class
 * @param tableName The table name
 * @param batchSize The number of entities in the batch
 * @param onFields The fields to use for the ON clause
 * @param ignoredFields The fields to ignore during updates
 * @param ignoreAllFields Whether to ignore all fields during updates
 */
data class CustomSqlCacheKey(
    val entityClass: Class<*>,
    val tableName: String,
    val batchSize: Int,
    val onFields: List<String>,
    val ignoredFields: List<String>,
    val ignoreAllFields: Boolean
)