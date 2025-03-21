package si.pecan.upsert.repository

import si.pecan.upsert.model.UpsertInfo

/**
 * Parser for upsert method names.
 * This class is responsible for parsing method names like "upsertAllOnAgendaIdAndTypeIgnoringUpdatedAt"
 * to extract fields for the ON clause and fields to ignore during updates.
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

        return UpsertInfo(methodName, isUpsertAll, onFields, ignoredFields, ignoreAllFields)
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
        // Pattern to match "On" followed by field names, stopping at "Ignoring"
        private val ON_PATTERN = Regex("On([A-Za-z0-9]+?)(?:Ignoring|$)")

        // Pattern to match "Ignoring" followed by field names
        private val IGNORING_PATTERN = Regex("Ignoring([A-Za-z0-9]+)$")

        // Pattern to match uppercase letters for camel case conversion
        private val CAMEL_CASE_PATTERN = Regex("([A-Z])")
    }
}

