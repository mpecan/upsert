package io.github.mpecan.upsert.bean

import io.github.mpecan.upsert.type.TypeMapper
import io.github.mpecan.upsert.type.TypeMapperRegistry
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Convert
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import java.lang.reflect.Field

/**
 * Extension of BeanPropertySqlParameterSource that supports additional types.
 * This class allows us to reuse Spring's existing infrastructure for parameter binding
 * while adding support for additional types and JPA converters.
 */
open class ExtendedBeanPropertySqlParameterSource(
    bean: Any,
    private val typeMapperRegistry: TypeMapperRegistry
) : BeanPropertySqlParameterSource(bean) {

    // Cache for fields by parameter name
    private val fieldCache = getFields(bean)

    // Cache for converter instances by converter class
    private val converterCache = mutableMapOf<Class<*>, AttributeConverter<Any, Any>>()

    // Cache for field annotations by field
    private val convertAnnotationCache = mutableMapOf<Field, Convert?>()

    /**
     * Override getSqlType to provide SQL types for additional Java/Kotlin types.
     * This method delegates to the TypeMapperRegistry to determine the SQL type.
     *
     * @param paramName The name of the parameter
     * @return The SQL type as defined in java.sql.Types
     */
    override fun getSqlType(paramName: String): Int {
        // Try to find the field in the bean class
        // Get the field from cache or find it
        return withField(paramName) { field ->
            // Use the injected registry if available, otherwise use the static methods
            typeMapperRegistry.getSqlTypeForField(field)
        } ?: return TypeMapper.getSqlTypeForValue(getValue(paramName))
    }

    /**
     * Override getValue to handle additional types that might need special conversion.
     * This method delegates to the TypeMapperRegistry to convert values to JDBC-compatible types.
     *
     * @param paramName The name of the parameter
     * @return The value of the parameter, possibly converted to a type that JDBC can handle
     */
    override fun getValue(paramName: String): Any? {
        // Get the value from the bean and handle null values
        val value = super.getValue(paramName) ?: return null

        // Get the field from cache or find it
        return withField(paramName) { field ->
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
                    converterClass.getDeclaredConstructor()
                        .newInstance() as AttributeConverter<Any, Any>
                }

                // Apply the converter to the value
                typedConverter.convertToDatabaseColumn(value)
            } else {

                // Use the injected registry if available, otherwise use the static methods
                typeMapperRegistry.convertToJdbcValue(value, field)
            }
        } ?: value
    }

    private inline fun <T : Any> withField(paramName: String, consumer: (Field) -> T?): T? {
        return fieldCache[paramName].let {
            try {
                if (it != null) consumer(it) else null
            } catch (e: Exception) {
                null
            }
        }
    }

    companion object {
        val typeFieldCache = mutableMapOf<Class<*>, Map<String, Field>>()

        fun getFields(bean: Any): Map<String, Field> {
            return typeFieldCache.getOrPut(bean.javaClass) {
                bean.javaClass.declaredFields.associateBy { it.name }
            }
        }
    }
}
