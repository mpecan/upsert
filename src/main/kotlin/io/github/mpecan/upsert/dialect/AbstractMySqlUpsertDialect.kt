package io.github.mpecan.upsert.dialect

import io.github.mpecan.upsert.bean.ExtendedBeanPropertySqlParameterSource
import io.github.mpecan.upsert.bean.IndexedBeanPropertySqlParameterSource
import io.github.mpecan.upsert.model.ColumnInfo
import io.github.mpecan.upsert.model.ConditionalInfo
import io.github.mpecan.upsert.model.UpsertModel
import io.github.mpecan.upsert.type.TypeMapperRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.PropertyAccessor
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

        val sql = generateBatchUpsertSql(
            upsertInstance.tableName,
            upsertInstance.onColumns,
            upsertInstance.values,
            upsertInstance.updateColumns,
            entities.size,
            upsertInstance.conditionalInfo
        )

        val allParamValues = createParameterSources(entities)
        val keyHolder = GeneratedKeyHolder()
        
        jdbcTemplate.update(sql, allParamValues, keyHolder)
        updateEntitiesWithGeneratedKeys(entities, keyHolder, upsertInstance.values)

        return entities
    }

    private fun <T : Any> createParameterSources(entities: List<T>): IndexedBeanPropertySqlParameterSource {
        return entities.map { entity ->
            ExtendedBeanPropertySqlParameterSource(entity, typeMapperRegistry)
        }.let { IndexedBeanPropertySqlParameterSource(it) }
    }

    private fun <T : Any> updateEntitiesWithGeneratedKeys(
        entities: List<T>,
        keyHolder: GeneratedKeyHolder,
        valueColumns: List<ColumnInfo>
    ) {
        val keysList = keyHolder.keyList.toList()
        if (keysList.isEmpty()) return

        val generatedColumns = valueColumns.filter { it.generated }
        entities.forEachIndexed { index, entity ->
            if (index < keysList.size) {
                updateEntityWithGeneratedKeys(entity, keysList[index], generatedColumns)
            }
        }
    }

    private fun <T : Any> updateEntityWithGeneratedKeys(
        entity: T,
        keys: Map<String, Any>,
        generatedColumns: List<ColumnInfo>
    ) {
        val beanWrapper = PropertyAccessorFactory.forDirectFieldAccess(entity)
        
        for (column in generatedColumns) {
            val generatedKey = findGeneratedKey(keys, column)
            if (generatedKey != null) {
                setGeneratedKeyOnEntity(beanWrapper, column.fieldName, generatedKey)
            }
        }
    }

    private fun findGeneratedKey(keys: Map<String, Any>, column: ColumnInfo): Any? {
        val possibleKeyNames = listOf("GENERATED_KEY", "GENERATED_KEYS", column.name, column.name.uppercase())
        return possibleKeyNames.firstNotNullOfOrNull { keyName -> keys[keyName] }
    }

    private fun setGeneratedKeyOnEntity(beanWrapper: PropertyAccessor, fieldName: String, generatedKey: Any) {
        try {
            if (beanWrapper.getPropertyValue(fieldName) == null) {
                beanWrapper.setPropertyValue(fieldName, generatedKey)
            }
        } catch (e: Exception) {
            logger.debug("Error setting generated key: ${e.message}")
        }
    }

    override fun getJsonType(): Int {
        return Types.VARCHAR
    }
}