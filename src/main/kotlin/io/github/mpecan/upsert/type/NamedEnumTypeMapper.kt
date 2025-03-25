package io.github.mpecan.upsert.type

import org.hibernate.annotations.JdbcTypeCode
import org.springframework.core.annotation.Order
import java.lang.reflect.Field

/**
 * Type mapper for enum values that converts them to their name strings.
 * This mapper is used for enums that should be stored as strings in the database.
 */
@Order(90)
class NamedEnumTypeMapper : TypeMapper {

    /**
     * Convert an enum value to its name string.
     *
     * @param value The enum value to convert
     * @return The name of the enum as a string, or the original value if not an enum
     */
    override fun convertToJdbcValue(value: Any?): Any? {
        if (value == null) {
            return null
        }
        if (value.javaClass.isEnum) {
            return (value as Enum<*>).name
        }
        return value
    }

    /**
     * Check if this mapper can handle the given field.
     * Handles enum fields that have a NAMED_ENUM JdbcTypeCode annotation.
     *
     * @param field The field to check
     * @return true if the field is an enum with NAMED_ENUM type annotation
     */
    override fun canHandle(field: Field): Boolean {
        return field.type.isEnum && field.getAnnotation(JdbcTypeCode::class.java)?.let {
            it.value == org.hibernate.type.SqlTypes.NAMED_ENUM
        } ?: false
    }

    /**
     * Check if this mapper can handle the given value.
     *
     * @param value The value to check
     * @return true if the value is an enum, false otherwise
     */
    override fun canHandleValue(value: Any?): Boolean {
        if (value == null) {
            return false
        }
        return value.javaClass.isEnum
    }
}