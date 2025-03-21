package io.github.mpecan.upsert.model

/**
 * Data class to hold information extracted from an upsert method name.
 *
 * @param isUpsertAll Whether the method is an upsertAll method
 * @param onFields The fields to use for the ON clause
 * @param ignoredFields The fields to ignore during updates
 * @param ignoreAllFields Whether to ignore all fields during updates
 */
data class UpsertInfo(
    val methodName: String,
    val isUpsertAll: Boolean,
    val onFields: List<String>,
    val ignoredFields: List<String>,
    val ignoreAllFields: Boolean
)