package io.github.mpecan.upsert.dialect

import io.github.mpecan.upsert.type.TypeMapperRegistry
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DatabaseMetaData
import javax.sql.DataSource

/**
 * Factory for creating the appropriate UpsertDialect based on the database being used.
 */
class UpsertDialectFactory(
    private val dataSource: DataSource,
    private val typeMapperRegistry: TypeMapperRegistry
) {

    val logger = LoggerFactory.getLogger(UpsertDialectFactory::class.java)

    /**
     * Get the appropriate UpsertDialect for the current database.
     *
     * @return The UpsertDialect implementation for the current database
     */
    fun getDialect(): UpsertDialect {
        return dataSource.connection.use { connection ->
            val metadata = connection.metaData
            when (getDatabaseType(connection)) {
                DatabaseType.POSTGRESQL -> PostgreSqlUpsertDialect(typeMapperRegistry)
                DatabaseType.MYSQL -> {
                    val isModernVersion = checkIfModernMySQLVersion(metadata)
                    // Use modern dialect for MySQL 8.0.19+
                    if (isModernVersion) {
                        MySqlUpsertDialect(typeMapperRegistry)
                    } else {
                        MySqlLegacyUpsertDialect(typeMapperRegistry)
                    }
                }

                else -> throw UnsupportedOperationException("Unsupported database type")
            }
        }
    }

    internal fun checkIfModernMySQLVersion(metadata: DatabaseMetaData): Boolean {
        try {
            val majorVersion = metadata.databaseMajorVersion
            val minorVersion = metadata.databaseMinorVersion
            val patchVersion =
                metadata.databaseProductVersion.substring("$majorVersion.$minorVersion.".length)
                    .toLong()
            val isModernVersion =
                majorVersion > 8 || (majorVersion == 8 && minorVersion > 0) || (majorVersion == 8 && minorVersion == 0 && patchVersion >= 19)
            return isModernVersion
        } catch (e: NumberFormatException) {
            logger.warn(
                "Could not parse database version from ${metadata.databaseProductVersion}",
                e
            )
            return true
        }
    }

    /**
     * Get the database type from the connection metadata.
     *
     * @param connection The database connection
     * @return The DatabaseType enum value
     */
    private fun getDatabaseType(connection: Connection): DatabaseType {
        val metadata = connection.metaData
        val databaseProductName = metadata.databaseProductName.lowercase()

        return when {
            databaseProductName.contains("postgresql") -> DatabaseType.POSTGRESQL
            databaseProductName.contains("mysql") -> DatabaseType.MYSQL
            else -> DatabaseType.UNKNOWN
        }
    }

    /**
     * Enum representing the supported database types.
     */
    enum class DatabaseType {
        POSTGRESQL,
        MYSQL,
        UNKNOWN
    }
}
