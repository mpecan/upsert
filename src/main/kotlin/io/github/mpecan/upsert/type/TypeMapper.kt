package io.github.mpecan.upsert.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Convert
import org.springframework.core.ResolvableType
import org.springframework.jdbc.core.StatementCreatorUtils
import java.lang.reflect.Field
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Interface for mapping Java/Kotlin types to SQL types.
 * This interface defines the contract for providing SQL type information and conversion logic for Java/Kotlin types.
 *
 * Implementations of this interface are responsible for:
 * 1. Determining if they can handle a specific field or value type
 * 2. Converting Java/Kotlin values to JDBC-compatible values
 * 3. Determining the appropriate SQL type for a field
 *
 * IMPORTANT: Every implementation must use the @Order annotation to define its own priority
 *
 * The system uses a registry of TypeMapper implementations to find the appropriate mapper for each field or value.
 */
interface TypeMapper {

    /**
     * Convert a value to a JDBC-compatible value.
     *
     * @param value The value to convert
     * @return The converted value that JDBC can handle
     */
    fun convertToJdbcValue(value: Any?): Any?

    /**
     * Check if this type mapper can handle the given Java/Kotlin class.
     *
     * @param field The field
     * @return true if this mapper can handle the type, false otherwise
     */
    fun canHandle(field: Field): Boolean

    /**
     * Check if this type mapper can handle the given value.
     *
     * @param value The value
     * @return true if this mapper can handle the value, false otherwise
     */
    fun canHandleValue(value: Any?): Boolean

    /**
     * Get the SQL type for a field, considering annotations like @Convert.
     * This method handles JPA's AttributeConverter mechanism by examining the converter's
     * target database type.
     *
     * @param field The field to determine the SQL type for
     * @return The SQL type as defined in java.sql.Types
     */
    fun getSqlTypeForField(field: Field): Int {
        // Check for @Convert annotation which indicates a JPA AttributeConverter is used
        val convertAnnotation = field.getAnnotation(Convert::class.java)
        if (convertAnnotation != null) {
            // Use Spring's ResolvableType to analyze the converter's generic type parameters
            val converterType = ResolvableType.forClass(convertAnnotation.converter.java)
                .`as`(AttributeConverter::class.java)

            // If we can resolve the converter's generic type parameters, use the database column type (second type parameter)
            if (converterType.hasGenerics()) {
                val targetType = converterType.getGeneric(1).resolve()
                if (targetType != null) {
                    return getSqlType(targetType)
                }
            }

            // If we can't resolve the type parameters, try to find the convertToDatabaseColumn method
            // and use its return type to determine the SQL type
            try {
                val method = convertAnnotation.converter.java.getDeclaredMethod(
                    "convertToDatabaseColumn",
                    Any::class.java
                )
                return getSqlType(method.returnType)
            } catch (e: Exception) {
                // Fall back to field type if we can't determine the converted type
            }
        }

        // If no converter is specified or we couldn't determine the converted type,
        // use the field's declared type
        return getSqlType(field.type)
    }

    companion object {
        /**
         * Determine the SQL type for a given Java/Kotlin class.
         * This method maps common Java/Kotlin types to their corresponding SQL types as defined in java.sql.Types.
         * For types not explicitly handled, it delegates to Spring's StatementCreatorUtils.
         *
         * @param javaType The Java/Kotlin class to map to an SQL type
         * @return The SQL type as defined in java.sql.Types
         */
        fun getSqlType(javaType: Class<*>): Int {
            return when {
                javaType == LocalDate::class.java -> Types.DATE
                javaType == LocalDateTime::class.java -> Types.TIMESTAMP
                javaType == LocalTime::class.java -> Types.TIME
                javaType == UUID::class.java -> Types.VARCHAR
                javaType.isEnum -> Types.VARCHAR
                javaType == String::class.java -> Types.VARCHAR
                javaType == Int::class.java || javaType == Integer::class.java -> Types.INTEGER
                javaType == Short::class.java || javaType == java.lang.Short::class.java -> Types.SMALLINT
                javaType == Byte::class.java || javaType == java.lang.Byte::class.java -> Types.TINYINT
                javaType == Long::class.java || javaType == java.lang.Long::class.java -> Types.BIGINT
                javaType == Float::class.java || javaType == java.lang.Float::class.java -> Types.FLOAT
                javaType == Double::class.java || javaType == java.lang.Double::class.java -> Types.DOUBLE
                javaType == Boolean::class.java || javaType == java.lang.Boolean::class.java -> Types.BOOLEAN
                javaType == ByteArray::class.java -> Types.VARBINARY
                javaType == java.sql.Date::class.java -> Types.DATE
                javaType == java.sql.Time::class.java -> Types.TIME
                javaType == java.sql.Timestamp::class.java -> Types.TIMESTAMP
                javaType == java.sql.Blob::class.java -> Types.BLOB
                javaType == java.sql.Clob::class.java -> Types.CLOB
                else -> StatementCreatorUtils.javaTypeToSqlParameterType(javaType)
            }
        }

        /**
         * Determine the SQL type for a given value.
         * This is a convenience method that handles null values and delegates to getSqlType for non-null values.
         *
         * @param value The value to determine the SQL type for
         * @return The SQL type as defined in java.sql.Types
         */
        fun getSqlTypeForValue(value: Any?): Int {
            if (value == null) {
                return Types.NULL
            }
            return getSqlType(value.javaClass)
        }

    }
}
