package io.github.mpecan.upsert.dialect

import io.github.mpecan.upsert.model.ColumnInfo
import io.github.mpecan.upsert.model.ConditionalInfo
import io.github.mpecan.upsert.type.TypeMapperRegistry

/**
 * MySQL implementation for version 8.0.19 and later.
 * Uses the modern alias syntax for conditional updates which avoids the VALUES() function limitations.
 * 
 * Example SQL:
 * ```sql
 * INSERT INTO table (id, name, version) VALUES (?, ?, ?) AS new_values
 * ON DUPLICATE KEY UPDATE 
 *   name = IF(new_values.version > version, new_values.name, name),
 *   version = IF(new_values.version > version, new_values.version, version)
 * ```
 */
class MySqlUpsertDialect(
    typeMapperRegistry: TypeMapperRegistry
) : AbstractMySqlUpsertDialect(typeMapperRegistry) {

    override fun generateInsertClause(
        tableName: String,
        valueColumns: List<ColumnInfo>,
        allPlaceholders: String,
        hasConditional: Boolean
    ): String {
        val columnList = valueColumns.joinToString(", ") { it.name }
        return if (hasConditional) {
            // Use alias syntax for conditional updates
            "INSERT INTO $tableName ($columnList) VALUES $allPlaceholders AS new_values"
        } else {
            // Standard syntax for non-conditional updates
            "INSERT INTO $tableName ($columnList) VALUES $allPlaceholders"
        }
    }

    override fun generateConditionalUpdateClause(
        tableName: String,
        updateColumns: List<ColumnInfo>,
        conditionalInfo: ConditionalInfo,
        valueColumns: List<ColumnInfo>
    ): String {
        val conditionalColumnName = resolveConditionalColumnName(conditionalInfo, valueColumns)
        val orderedColumns = orderColumnsForConditionalUpdate(updateColumns, conditionalInfo)
        
        // Use the modern alias syntax which doesn't suffer from VALUES() limitations
        return orderedColumns.joinToString(", ") { column ->
            "${column.name} = IF(new_values.$conditionalColumnName ${conditionalInfo.operator.sqlOperator} $tableName.$conditionalColumnName, new_values.${column.name}, $tableName.${column.name})"
        }
    }
}