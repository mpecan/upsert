package io.github.mpecan.upsert.type

import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.lang.reflect.Field
import java.sql.Types

/**
 * Interface for JSON-specific type mapping and conversion.
 * Implementations provide JSON serialization for complex objects.
 */
interface JsonTypeMapper : TypeMapper {

    /**
     * Serialize an object to its JSON string representation.
     *
     * @param value The value to serialize
     * @return The JSON string representation
     */
    fun toJson(value: Any): String

    /**
     * The SQL type to use for JSON values.
     * Defaults to Types.OTHER which is appropriate for most databases' JSON types.
     */
    val sqlType: Int
        get() = Types.OTHER

    override fun convertToJdbcValue(value: Any?): Any? =
        if (value == null) null else toJson(value)

    override fun getSqlTypeForField(field: Field): Int =
        if (canHandle(field)) sqlType else DefaultTypeMapper().getSqlTypeForField(field)
}

/**
 * Abstract base class for JSON type mappers.
 * Provides common functionality for all JSON mapper implementations.
 */
abstract class AbstractJsonTypeMapper(override val sqlType: Int) : JsonTypeMapper {

    /**
     * Check if this mapper can handle the given value.
     * Uses heuristics to determine if a value is likely to be serialized as JSON.
     *
     * @param value The value to check
     * @return true if the value is likely to be serialized as JSON, false otherwise
     */
    override fun canHandleValue(value: Any?): Boolean {
        if (value == null) return false
        return isLikelyJsonType(value.javaClass)
    }

    /**
     * Check if this mapper can handle the given field.
     * Looks for JSON indicators in column definitions and annotations.
     *
     * @param field The field to check
     * @return true if the field is likely to contain JSON data, false otherwise
     */
    override fun canHandle(field: Field): Boolean {
        return isJsonField(field)
    }

    /**
     * Check if a field should be treated as JSON.
     * Looks for JSON indicators in column definitions and annotations.
     *
     * @param field The field to check
     * @return true if the field should be treated as JSON, false otherwise
     */
    fun isJsonField(field: Field): Boolean {
        // Check for column definition that indicates JSON
        val columnAnn = field.getAnnotation(jakarta.persistence.Column::class.java)
        if (columnAnn != null && columnAnn.columnDefinition.isNotBlank()) {
            val columnDefinition = columnAnn.columnDefinition.lowercase()
            if (columnDefinition.contains("json") || columnDefinition.contains("jsonb")) {
                return true
            }
        }
        val sqlTypeAnn = field.getAnnotation(JdbcTypeCode::class.java)
        if (sqlTypeAnn != null) {
            return sqlTypeAnn.value == SqlTypes.JSON
        }
        // Check if the field type is likely to be serialized as JSON
        return isLikelyJsonType(field.type)
    }

    /**
     * Check if a type is likely to be serialized as JSON.
     *
     * @param clazz The class to check
     * @return true if the class is likely JSON data, false otherwise
     */
    private fun isLikelyJsonType(clazz: Class<*>): Boolean {
        // Standard JSON container types
        if (Map::class.java.isAssignableFrom(clazz) ||
            Collection::class.java.isAssignableFrom(clazz)
        ) {
            return true
        }

        // Exclude primitive types, standard JPA types, and common non-JSON types
        if (clazz.isPrimitive ||
            clazz.name.startsWith("java.lang") ||
            clazz.name.startsWith("java.time") ||
            clazz.name.startsWith("java.sql") ||
            clazz.name.startsWith("java.math") ||
            clazz.isEnum ||
            clazz == String::class.java ||
            clazz == ByteArray::class.java ||
            clazz == CharArray::class.java
        ) {
            return false
        }

        // For other custom classes, check if they're likely to be data objects
        // that should be serialized as JSON
        return !clazz.name.startsWith("java.") &&
                !clazz.name.startsWith("javax.") &&
                !clazz.name.startsWith("jakarta.")
    }
}
