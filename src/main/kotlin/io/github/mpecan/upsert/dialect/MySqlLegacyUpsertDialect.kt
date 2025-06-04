package io.github.mpecan.upsert.dialect

import io.github.mpecan.upsert.model.ColumnInfo
import io.github.mpecan.upsert.model.ConditionalInfo
import io.github.mpecan.upsert.type.TypeMapperRegistry

/**
 * MySQL implementation for versions before 8.0.19.
 * Uses the VALUES() function syntax which has known limitations with conditional updates.
 * 
 * IMPORTANT LIMITATIONS:
 * 1. The VALUES() function has critical issues when used in multiple conditional expressions
 * 2. Field order in the UPDATE clause matters - fields used in conditions may see updated values
 * 3. Numeric columns may not update correctly in multi-column conditional updates
 * 4. VALUES() was deprecated in MySQL 8.0.20 due to these architectural issues
 * 
 * For reliable conditional updates, consider:
 * - Using stored procedures
 * - Multiple SQL statements (SELECT + UPDATE) in a transaction
 * - Upgrading to MySQL 8.0.19+ to use MySqlModernUpsertDialect
 * 
 * See: 
 * - https://bugs.mysql.com/bug.php?id=88657
 * - https://dev.mysql.com/worklog/task/?id=6312
 */
class MySqlLegacyUpsertDialect(
    typeMapperRegistry: TypeMapperRegistry
) : AbstractMySqlUpsertDialect(typeMapperRegistry) {

    override fun generateInsertClause(
        tableName: String,
        valueColumns: List<ColumnInfo>,
        allPlaceholders: String,
        hasConditional: Boolean
    ): String {
        val columnList = valueColumns.joinToString(", ") { it.name }
        // Legacy syntax doesn't support aliases
        return "INSERT INTO $tableName ($columnList) VALUES $allPlaceholders"
    }

    override fun generateConditionalUpdateClause(
        tableName: String,
        updateColumns: List<ColumnInfo>,
        conditionalInfo: ConditionalInfo,
        valueColumns: List<ColumnInfo>
    ): String {
        val conditionalColumnName = resolveConditionalColumnName(conditionalInfo, valueColumns)
        val orderedColumns = orderColumnsForConditionalUpdate(updateColumns, conditionalInfo)
        
        // Use VALUES() function with known limitations
        logger.warn(
            "Using legacy MySQL VALUES() syntax for conditional updates. " +
            "This has known limitations and may not work correctly for all data types. " +
            "Consider upgrading to MySQL 8.0.19+ for reliable conditional updates."
        )
        
        return orderedColumns.joinToString(", ") { column ->
            "${column.name} = IF(VALUES($conditionalColumnName) ${conditionalInfo.operator.sqlOperator} $conditionalColumnName, VALUES(${column.name}), ${column.name})"
        }
    }
}