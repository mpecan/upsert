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
 * This interface defines the contract for providing SQL type information for Java/Kotlin types.
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
     *
     * @param field The field
     * @return The SQL type as defined in java.sql.Types
     */
    fun getSqlTypeForField(field: Field): Int {
        // Check for @Convert annotation
        val convertAnnotation = field.getAnnotation(Convert::class.java)
        if (convertAnnotation != null) {
            val converterType = ResolvableType.forClass(convertAnnotation.converter.java)
                .`as`(AttributeConverter::class.java)

            // If we can resolve the converter type parameters, use the target type
            if (converterType.hasGenerics()) {
                val targetType = converterType.getGeneric(1).resolve()
                if (targetType != null) {
                    return getSqlType(targetType)
                }
            }

            // Fall back to the converter class's convertToDatabaseColumn method return type
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

        return getSqlType(field.type)
    }

    companion object {
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

        fun getSqlTypeForValue(value: Any?): Int {
            if (value == null) {
                return Types.NULL
            }
            return getSqlType(value.javaClass)
        }

    }
}
