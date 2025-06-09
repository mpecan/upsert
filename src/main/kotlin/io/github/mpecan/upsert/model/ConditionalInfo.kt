package io.github.mpecan.upsert.model

/**
 * Enum representing comparison operators for conditional upserts.
 */
enum class ComparisonOperator(val sqlOperator: String) {
    MORE(">"),
    MORE_OR_EQUAL(">="),
    LESS("<"),
    LESS_OR_EQUAL("<=")
}

/**
 * Data class to hold information about conditional clauses in upsert operations.
 *
 * @param fieldName The field name to compare (e.g., "updated_at", "version")
 * @param operator The comparison operator to use
 */
data class ConditionalInfo(
    val fieldName: String,
    val operator: ComparisonOperator
)