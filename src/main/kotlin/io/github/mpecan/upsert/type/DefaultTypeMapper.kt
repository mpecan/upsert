package io.github.mpecan.upsert.type

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import java.lang.reflect.Field

/**
 * Default implementation of TypeMapper.
 * This class handles most standard Java/Kotlin types and serves as a fallback when no other TypeMapper can handle a type.
 *
 * The @Order(Ordered.LOWEST_PRECEDENCE) annotation ensures this mapper is used only when no other mapper
 * with higher precedence can handle the type. This allows more specific mappers to take precedence
 * while still providing a fallback for all other types.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
class DefaultTypeMapper : TypeMapper {

    /**
     * Check if this type mapper can handle the given field.
     * The default mapper handles all fields as a fallback.
     *
     * @param field The field to check
     * @return Always returns true as this is the default mapper
     */
    override fun canHandle(field: Field): Boolean {
        return true // Default mapper handles all fields
    }

    /**
     * Check if this type mapper can handle the given value.
     * The default mapper handles all values as a fallback.
     *
     * @param value The value to check
     * @return Always returns true as this is the default mapper
     */
    override fun canHandleValue(value: Any?): Boolean {
        return true // Default mapper handles all values
    }

    /**
     * Convert a value to a JDBC-compatible value.
     * This implementation specifically handles enum types by converting them to their name strings.
     * For all other types, it returns the value unchanged, relying on JDBC's built-in type handling.
     *
     * @param value The value to convert
     * @return The converted value that JDBC can handle
     */
    override fun convertToJdbcValue(value: Any?): Any? {
        if (value == null) {
            return null
        }

        return when (value) {
            is Enum<*> -> value.name
            else -> value
        }
    }


}
