package si.pecan.upsert.repository

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Extension of BeanPropertySqlParameterSource that supports additional types.
 * This class allows us to reuse Spring's existing infrastructure for parameter binding
 * while adding support for additional types that might be used in our entities.
 */
class ExtendedBeanPropertySqlParameterSource(bean: Any) : BeanPropertySqlParameterSource(bean) {

    /**
     * Override getSqlType to provide SQL types for additional Java/Kotlin types.
     * This method is called by Spring's JdbcTemplate to determine the SQL type of a parameter.
     *
     * @param paramName The name of the parameter
     * @return The SQL type as defined in java.sql.Types
     */
    override fun getSqlType(paramName: String): Int {
        val value = getValue(paramName)
        return when (value) {
            // Handle additional types here
            is LocalDate -> Types.DATE
            is LocalDateTime -> Types.TIMESTAMP
            is LocalTime -> Types.TIME
            is UUID -> Types.VARCHAR
            is Enum<*> -> Types.VARCHAR
            // Add more type mappings as needed
            else -> super.getSqlType(paramName)
        }
    }

    /**
     * Override getValue to handle additional types that might need special conversion.
     * This method is called by Spring's JdbcTemplate to get the value of a parameter.
     *
     * @param paramName The name of the parameter
     * @return The value of the parameter, possibly converted to a type that JDBC can handle
     */
    override fun getValue(paramName: String): Any? {
        val value = super.getValue(paramName)
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