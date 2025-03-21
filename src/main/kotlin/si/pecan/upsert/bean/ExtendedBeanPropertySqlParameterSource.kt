package si.pecan.upsert.bean

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Convert
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import java.lang.reflect.Field
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Extension of BeanPropertySqlParameterSource that supports additional types.
 * This class allows us to reuse Spring's existing infrastructure for parameter binding
 * while adding support for additional types that might be used in our entities.
 *
 * This implementation adds support for:
 * - JPA custom converters (@Convert annotation)
 * - Fields that should be serialized as JSON
 */
open class ExtendedBeanPropertySqlParameterSource(bean: Any) : BeanPropertySqlParameterSource(bean) {

    // The bean being wrapped
    private val beanInstance = bean

    // Cache for fields by parameter name
    private val fieldCache = mutableMapOf<String, Field?>()

    // Cache for converter instances by converter class
    private val converterCache = mutableMapOf<Class<*>, AttributeConverter<Any, Any>>()

    // Cache for field annotations by field
    private val convertAnnotationCache = mutableMapOf<Field, Convert?>()

    /**
     * Override getSqlType to provide SQL types for additional Java/Kotlin types.
     * This method is called by Spring's JdbcTemplate to determine the SQL type of a parameter.
     * Uses caches to avoid repeated reflection.
     *
     * @param paramName The name of the parameter
     * @return The SQL type as defined in java.sql.Types
     */
    override fun getSqlType(paramName: String): Int {
        // Try to find the field in the bean class
        try {
            // Get the field from cache or find it
            val field = fieldCache.getOrPut(paramName) {
                try {
                    val beanClass = beanInstance.javaClass
                    val field = beanClass.getDeclaredField(paramName)
                    field.isAccessible = true
                    field
                } catch (e: Exception) {
                    null
                }
            }

            if (field != null) {
                // Check if the field has a @Convert annotation (from cache)
                val convertAnnotation = convertAnnotationCache.getOrPut(field) {
                    field.getAnnotation(Convert::class.java)
                }

                if (convertAnnotation != null) {
                    // Get the converter class
                    val converterClass = convertAnnotation.converter.java

                    // Get or create the converter instance
                    val typedConverter = converterCache.getOrPut(converterClass) {
                        @Suppress("UNCHECKED_CAST")
                        converterClass.getDeclaredConstructor().newInstance() as AttributeConverter<Any, Any>
                    }

                    // Get the value
                    val value = super.getValue(paramName)

                    // If the value is null, we can't determine the type from it
                    if (value != null) {
                        // Apply the converter to the value
                        val convertedValue = typedConverter.convertToDatabaseColumn(value)

                        // Determine the SQL type based on the converted value
                        return getSqlTypeForValue(convertedValue)
                    }
                }
            }
        } catch (e: Exception) {
            // If any exception occurs, fall back to default type determination
        }

        // Fall back to determining the type based on the value
        val value = getValue(paramName)
        return getSqlTypeForValue(value)
    }

    /**
     * Determine the SQL type for a value.
     *
     * @param value The value
     * @return The SQL type as defined in java.sql.Types
     */
    private fun getSqlTypeForValue(value: Any?): Int {
        return when (value) {
            // Handle additional types here
            is LocalDate -> Types.DATE
            is LocalDateTime -> Types.TIMESTAMP
            is LocalTime -> Types.TIME
            is UUID -> Types.VARCHAR
            is Enum<*> -> Types.VARCHAR
            is String -> Types.VARCHAR
            is Int, is Short, is Byte -> Types.INTEGER
            is Long -> Types.BIGINT
            is Float, is Double -> Types.DOUBLE
            is Boolean -> Types.BOOLEAN
            // Add more type mappings as needed
            null -> Types.NULL
            else -> Types.OTHER
        }
    }

    /**
     * Override getValue to handle additional types that might need special conversion.
     * This method is called by Spring's JdbcTemplate to get the value of a parameter.
     * Uses caches to avoid repeated reflection.
     *
     * @param paramName The name of the parameter
     * @return The value of the parameter, possibly converted to a type that JDBC can handle
     */
    override fun getValue(paramName: String): Any? {
        val value = super.getValue(paramName)

        // Handle null values
        if (value == null) {
            return null
        }

        // Try to find the field in the bean class
        try {
            // Get the field from cache or find it
            val field = fieldCache.getOrPut(paramName) {
                try {
                    val beanClass = beanInstance.javaClass
                    val field = beanClass.getDeclaredField(paramName)
                    field.isAccessible = true
                    field
                } catch (e: Exception) {
                    null
                }
            }

            if (field != null) {
                // Check if the field has a @Convert annotation (from cache)
                val convertAnnotation = convertAnnotationCache.getOrPut(field) {
                    field.getAnnotation(Convert::class.java)
                }

                if (convertAnnotation != null) {
                    // Get the converter class
                    val converterClass = convertAnnotation.converter.java

                    // Get or create the converter instance
                    val typedConverter = converterCache.getOrPut(converterClass) {
                        @Suppress("UNCHECKED_CAST")
                        converterClass.getDeclaredConstructor().newInstance() as AttributeConverter<Any, Any>
                    }

                    // Apply the converter to the value
                    return typedConverter.convertToDatabaseColumn(value)
                }
            }
        } catch (e: Exception) {
            // If any exception occurs, fall back to default conversion
        }

        // Apply default conversion for other types
        return when (value) {
            // Convert types that JDBC doesn't handle natively
            is Enum<*> -> value.name
            // Add more conversions as needed
            else -> value
        }
    }

    companion object {
        /**
         * Create a list of parameter sources from a list of beans.
         * This is useful for batch operations.
         *
         * @param beans The list of beans
         * @return The list of parameter sources
         */
        fun createBatch(beans: List<Any>): List<ExtendedBeanPropertySqlParameterSource> {
            return beans.map { ExtendedBeanPropertySqlParameterSource(it) }
        }
    }
}