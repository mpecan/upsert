package io.github.mpecan.upsert.type

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import java.lang.reflect.Field
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Default implementation of TypeMapper.
 * This class handles most standard Java/Kotlin types and delegates to Spring's StatementCreatorUtils for others.
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
     * Handles common Java/Kotlin types like enums, dates, and times.
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
            is LocalDate -> java.sql.Date.valueOf(value)
            is LocalDateTime -> java.sql.Timestamp.valueOf(value)
            is LocalTime -> java.sql.Time.valueOf(value)
            else -> value
        }
    }


}
