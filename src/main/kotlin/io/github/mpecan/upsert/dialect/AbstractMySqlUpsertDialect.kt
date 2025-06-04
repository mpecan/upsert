package io.github.mpecan.upsert.dialect

import io.github.mpecan.upsert.bean.ExtendedBeanPropertySqlParameterSource
import io.github.mpecan.upsert.bean.IndexedBeanPropertySqlParameterSource
import io.github.mpecan.upsert.model.ColumnInfo
import io.github.mpecan.upsert.model.ConditionalInfo
import io.github.mpecan.upsert.model.UpsertModel
import io.github.mpecan.upsert.type.TypeMapperRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.PropertyAccessorFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Types

/**
 * Abstract base implementation for MySQL upsert dialects.
 * Contains common functionality shared between different MySQL versions.
 */
abstract class AbstractMySqlUpsertDialect(
    protected val typeMapperRegistry: TypeMapperRegistry
) : UpsertDialect {

    protected val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Generate the UPDATE clause for conditional updates.
     * Different MySQL versions may use different syntax (VALUES() vs alias).
     */
    abstract fun generateConditionalUpdateClause(
        tableName: String,
        updateColumns: List<ColumnInfo>,
        conditionalInfo: ConditionalInfo,
        valueColumns: List<ColumnInfo>
    ): String

    /**
     * Generate the INSERT clause. Modern MySQL supports alias syntax.
     */
    abstract fun generateInsertClause(
        tableName: String,
        valueColumns: List<ColumnInfo>,
        allPlaceholders: String,
        hasConditional: Boolean
    ): String

    /**
     * Orders columns to ensure conditional field is updated last.
     * This prevents issues where the conditional field affects other conditions during the update.
     */
    protected fun orderColumnsForConditionalUpdate(
        updateColumns: List<ColumnInfo>,
        conditionalInfo: ConditionalInfo
    ): List<ColumnInfo> {
        val isConditionalFieldUpdated = updateColumns.any { it.fieldName == conditionalInfo.fieldName }
        
        return if (isConditionalFieldUpdated) {
            // Place conditional field at the end to avoid it affecting other conditions
            updateColumns.filter { it.fieldName != conditionalInfo.fieldName } +
                    updateColumns.filter { it.fieldName == conditionalInfo.fieldName }
        } else {
            updateColumns
        }
    }

    /**
     * Finds the column name for the conditional field from the value columns.
     */
    protected fun resolveConditionalColumnName(
        conditionalInfo: ConditionalInfo,
        valueColumns: List<ColumnInfo>
    ): String {
        val conditionalColumnInfo = valueColumns.find { 
            it.fieldName == conditionalInfo.fieldName || it.name == conditionalInfo.fieldName 
        }
        return conditionalColumnInfo?.name ?: conditionalInfo.fieldName
    }

    override fun generateBatchUpsertSql(
        tableName: String,
        keyColumns: List<ColumnInfo>,
        valueColumns: List<ColumnInfo>,
        updateColumns: List<ColumnInfo>,
        batchSize: Int,
        conditionalInfo: ConditionalInfo?
    ): String {
        // Create placeholders for all entities in the batch
        val allPlaceholders = (1..batchSize).joinToString(", ") {
            "(${valueColumns.joinToString(", ") { column -> ":${column.fieldName}_${it}" }})"
        }

        val insertClause = generateInsertClause(tableName, valueColumns, allPlaceholders, conditionalInfo != null)

        val updateClause = when {
            updateColumns.isEmpty() -> {
                // If no columns to update, use a dummy update that effectively does nothing
                if (keyColumns.isNotEmpty()) {
                    "${keyColumns.first().name} = ${keyColumns.first().name}"
                } else {
                    "1=1"
                }
            }
            conditionalInfo != null -> {
                generateConditionalUpdateClause(tableName, updateColumns, conditionalInfo, valueColumns)
            }
            else -> {
                updateColumns.joinToString(", ") { "${it.name} = VALUES(${it.name})" }
            }
        }

        return "$insertClause ON DUPLICATE KEY UPDATE $updateClause"
    }

    override fun <T : Any> upsertData(
        upsertInstance: UpsertModel.UpsertInstance,
        entities: List<T>,
        jdbcTemplate: NamedParameterJdbcTemplate
    ): List<T> {
        if (entities.isEmpty()) {
            return emptyList()
        }

        // Generate the SQL for the batch
        val sql = generateBatchUpsertSql(
            upsertInstance.tableName,
            upsertInstance.onColumns,
            upsertInstance.values,
            upsertInstance.updateColumns,
            entities.size,
            upsertInstance.conditionalInfo
        )

        // For MySQL, we need to create parameter sources with indexed names
        val allParamValues = entities.mapIndexed { index, entity ->
            ExtendedBeanPropertySqlParameterSource(entity, typeMapperRegistry)
        }.let { IndexedBeanPropertySqlParameterSource(it) }

        val keyHolder = GeneratedKeyHolder()
        
        // Execute the SQL with all parameters using named parameters
        jdbcTemplate.update(sql, allParamValues, keyHolder)

        // Update entities with generated keys if needed
        val keysList = keyHolder.keyList

        if (keysList.isNotEmpty()) {
            // Find generated columns
            val generatedColumns = upsertInstance.values.filter { it.generated }

            entities.forEachIndexed { index, entity ->
                if (index < keysList.size) {
                    val keys = keysList[index]
                    val beanWrapper = PropertyAccessorFactory.forDirectFieldAccess(entity)
                    for (column in generatedColumns) {
                        // Try different key names that might be used by the database
                        val possibleKeyNames = listOf("GENERATED_KEY", "GENERATED_KEYS", column.name, column.name.uppercase())
                        var generatedKey: Any? = null

                        for (keyName in possibleKeyNames) {
                            generatedKey = keys[keyName]
                            if (generatedKey != null) {
                                break
                            }
                        }

                        if (generatedKey != null) {
                            try {
                                if (beanWrapper.getPropertyValue(column.fieldName) != null) {
                                    // Skip setting the generated key if the field is already set
                                    continue
                                }
                                beanWrapper.setPropertyValue(column.fieldName, generatedKey)
                            } catch (e: Exception) {
                                // Log the error but continue processing
                                logger.debug("Error setting generated key: ${e.message}")
                            }
                        }
                    }
                }
            }
        }

        return entities
    }

    override fun getJsonType(): Int {
        return Types.VARCHAR
    }
}