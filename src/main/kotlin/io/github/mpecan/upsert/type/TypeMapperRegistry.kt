package io.github.mpecan.upsert.type

import org.hibernate.annotations.JdbcTypeCode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component
import java.lang.reflect.Field

/**
 * Registry for type mappers.
 * This class allows registering custom type mappers and provides methods to find the appropriate mapper for a type.
 */
@Component
class TypeMapperRegistry(private val mapperListProvider: ObjectProvider<List<TypeMapper>>) {
    private val logger = LoggerFactory.getLogger(TypeMapperRegistry::class.java)
    private val typeMappers: List<TypeMapper> by lazy { mapperListProvider.getObject() }


    /**
     * Convert a value to a JDBC-compatible value using the appropriate type mapper for the field.
     *
     * @param value The value to convert
     * @param field The field associated with the value
     * @return The converted value that JDBC can handle
     */
    fun convertToJdbcValue(value: Any?, field: Field): Any? {
        if (value == null) {
            return null
        }

        try {
            val mapper = forField(field)
            logger.trace(
                "Converting value of type {} using mapper {}",
                value.javaClass.simpleName, mapper.javaClass.simpleName
            )
            return mapper.convertToJdbcValue(value)
        } catch (e: Exception) {
            logger.warn(
                "Error converting value of type {} for field {}: {}",
                value.javaClass.simpleName, field.name, e.message
            )
            // Fall back to returning the original value if conversion fails
            return value
        }
    }

    private fun forField(field: Field): TypeMapper {
        val mapper = typeMappers.firstOrNull { mapper ->
            try {
                mapper.canHandle(field)
            } catch (e: Exception) {
                logger.warn(
                    "Error checking if mapper {} can handle field {}: {}",
                    mapper.javaClass.simpleName, field.name, e.message
                )
                false
            }
        }

        if (mapper == null) {
            logger.debug(
                "No specific mapper found for field {}, using DefaultTypeMapper",
                field.name
            )
            return DefaultTypeMapper()
        }

        logger.trace("Found mapper {} for field {}", mapper.javaClass.simpleName, field.name)
        return mapper
    }

    /**
     * Get the SQL type for a field, considering annotations like @Convert.
     *
     * @param field The field
     * @return The SQL type as defined in java.sql.Types
     */
    fun getSqlTypeForField(field: Field): Int {
        try {
            // Check if the field has an explicit SQL type
            val sqlTypeAnn =
                field.annotations.firstOrNull { it.javaClass == JdbcTypeCode::class.java } as? JdbcTypeCode
            if (sqlTypeAnn != null) {
                logger.trace(
                    "Found explicit JdbcTypeCode annotation for field {}: {}",
                    field.name,
                    sqlTypeAnn.value
                )
                return sqlTypeAnn.value
            }

            // Find first mapper that can handle the field type
            val mapper = forField(field)
            try {
                val sqlType = mapper.getSqlTypeForField(field)
                logger.trace(
                    "Determined SQL type for field {} using mapper {}: {}",
                    field.name, mapper.javaClass.simpleName, sqlType
                )
                return sqlType
            } catch (e: Exception) {
                logger.warn(
                    "Error getting SQL type for field {} using mapper {}: {}",
                    field.name, mapper.javaClass.simpleName, e.message
                )
                // Fall back to default SQL type determination
                val defaultType = TypeMapper.getSqlType(field.type)
                logger.debug(
                    "Falling back to default SQL type for field {}: {}",
                    field.name,
                    defaultType
                )
                return defaultType
            }
        } catch (e: Exception) {
            logger.error(
                "Unexpected error determining SQL type for field {}: {}",
                field.name,
                e.message
            )
            // Last resort fallback
            return java.sql.Types.OTHER
        }
    }

    /**
     * Get all registered mappers.
     * This is primarily used for testing.
     *
     * @return List of all registered mappers
     */
    fun getRegisteredMappers(): List<TypeMapper> {
        return typeMappers.toList()
    }
}
