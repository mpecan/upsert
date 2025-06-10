package io.github.mpecan.upsert.type

import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.core.annotation.Order
import java.lang.reflect.Field
import java.sql.Types

/**
 * Type mapper for enum values that converts them to their ordinal values.
 * This mapper is used for enums that should be stored as integers in the database.
 */
@Order(100)
class EnumTypeMapper : TypeMapper {

    /**
     * Convert an enum value to its ordinal value.
     *
     * @param value The enum value to convert
     * @return The ordinal value of the enum, or the original value if not an enum
     */
    override fun convertToJdbcValue(value: Any?): Any? {
        if (value == null) {
            return null
        }
        if (value.javaClass.isEnum) {
            return (value as Enum<*>).ordinal
        }
        return value
    }

    /**
     * Check if this mapper can handle the given field.
     * Handles enum fields that have an ORDINAL_ENUM JdbcTypeCode annotation or no annotation.
     *
     * @param field The field to check
     * @return true if the field is an enum with ORDINAL_ENUM type annotation or no annotation
     */
    override fun canHandle(field: Field): Boolean {
        if (!field.type.isEnum) {
            return false
        }

        val jdbcTypeCode = field.getAnnotation(JdbcTypeCode::class.java)
        if (jdbcTypeCode == null) {
            return true // Handle enums with no annotation
        }

        return jdbcTypeCode.value == org.hibernate.type.SqlTypes.ORDINAL_ENUM
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

