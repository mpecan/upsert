package io.github.mpecan.upsert.repository

import io.github.mpecan.upsert.model.ComparisonOperator
import io.github.mpecan.upsert.model.ConditionalInfo
import io.github.mpecan.upsert.model.UpsertInfo

/**
 * Parser for upsert method names.
 * This class is responsible for parsing method names like "upsertAllOnAgendaIdAndTypeWhenUpdatedAtMoreIgnoringCreatedAt"
 * to extract fields for the ON clause, conditional clauses, and fields to ignore during updates.
 * 
 * Supported patterns:
 * - upsert[All]On&lt;fields&gt;[When&lt;field&gt;&lt;operator&gt;][Ignoring&lt;fields&gt;]
 * - Operators: More (&gt;), MoreOrEqual (&gt;=), Less (&lt;), LessOrEqual (&lt;=)
 */
class UpsertMethodNameParser {

    /**
     * Parse a method name to extract upsert information.
     *
     * @param methodName The method name to parse
     * @return The parsed upsert information, or null if the method name is not an upsert method
     */
    fun parse(methodName: String): UpsertInfo? {
        // Check if the method name starts with "upsert"
        if (!methodName.startsWith("upsert")) {
            return null
        }

        // Default values
        var isUpsertAll = false
        val onFields = mutableListOf<String>()
        val ignoredFields = mutableListOf<String>()
        var ignoreAllFields = false
        var conditionalInfo: ConditionalInfo? = null

        // Check if it's an upsertAll method
        if (methodName.startsWith("upsertAll")) {
            isUpsertAll = true
        }

        // Extract fields for the ON clause
        val onMatch = ON_PATTERN.find(methodName)
        if (onMatch != null) {
            val onPart = onMatch.groupValues[1]
            onFields.addAll(splitCamelCase(onPart))
        }

        // Extract conditional information (When clause)
        val whenMatch = WHEN_PATTERN.find(methodName)
        if (whenMatch != null) {
            val fieldAndOperator = whenMatch.groupValues[1]
            conditionalInfo = parseConditionalClause(fieldAndOperator)
        }

        // Extract fields to ignore during updates
        val ignoringMatch = IGNORING_PATTERN.find(methodName)
        if (ignoringMatch != null) {
            val ignoringPart = ignoringMatch.groupValues[1]
            if (ignoringPart == "AllFields") {
                ignoreAllFields = true
            } else {
                ignoredFields.addAll(splitCamelCase(ignoringPart))
            }
        }

        return UpsertInfo(methodName, isUpsertAll, onFields, ignoredFields, ignoreAllFields, conditionalInfo)
    }

    /**
     * Parse conditional clause from a When part of the method name.
     * For example, "UpdatedAtMore" becomes ConditionalInfo("updated_at", ComparisonOperator.MORE).
     *
     * @param fieldAndOperator The field and operator string to parse
     * @return The conditional information, or null if parsing fails
     */
    private fun parseConditionalClause(fieldAndOperator: String): ConditionalInfo? {
        // Try to match each operator pattern
        for ((operator, pattern) in OPERATOR_PATTERNS) {
            val match = pattern.find(fieldAndOperator)
            if (match != null) {
                val fieldPart = match.groupValues[1]
                // Keep the original camelCase field name for entity property lookup
                // The dialect will handle conversion to snake_case for SQL
                return ConditionalInfo(fieldPart.replaceFirstChar { it.lowercase() }, operator)
            }
        }
        return null
    }

    /**
     * Split a camel case string into a list of strings.
     * For example, "agendaIdAndType" becomes ["agenda_id", "type"].
     *
     * @param camelCase The camel case string to split
     * @return The list of strings
     */
    private fun splitCamelCase(camelCase: String): List<String> {
        // Split on "And" to get individual field names
        val parts = camelCase.split("And")

        // Convert each part to snake_case
        return parts.map { part ->
            // Insert underscore before each uppercase letter and convert to lowercase
            part.replace(CAMEL_CASE_PATTERN, "_$1").lowercase()
                // Remove leading underscore if present
                .removePrefix("_")
        }
    }

    companion object {
        // Pattern to match "On" followed by field names, stopping at "When" or "Ignoring"
        private val ON_PATTERN = Regex("On([A-Za-z0-9]+?)(?:When|Ignoring|$)")

        // Pattern to match "When" followed by field and operator
        private val WHEN_PATTERN = Regex("When([A-Za-z0-9]+?)(?:Ignoring|$)")

        // Pattern to match "Ignoring" followed by field names
        private val IGNORING_PATTERN = Regex("Ignoring([A-Za-z0-9]+)$")

        // Pattern to match uppercase letters for camel case conversion
        private val CAMEL_CASE_PATTERN = Regex("([A-Z])")

        // Map of comparison operators to their regex patterns
        // Order matters: check longer patterns first (MoreOrEqual before More)
        private val OPERATOR_PATTERNS = listOf(
            ComparisonOperator.MORE_OR_EQUAL to Regex("(.+)MoreOrEqual$"),
            ComparisonOperator.LESS_OR_EQUAL to Regex("(.+)LessOrEqual$"),
            ComparisonOperator.MORE to Regex("(.+)More$"),
            ComparisonOperator.LESS to Regex("(.+)Less$")
        )
    }
}

