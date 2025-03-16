package si.pecan.upsert.dialect

import java.sql.Connection
import javax.sql.DataSource

/**
 * Factory for creating the appropriate UpsertDialect based on the database being used.
 */
class UpsertDialectFactory(private val dataSource: DataSource) {

    /**
     * Get the appropriate UpsertDialect for the current database.
     *
     * @return The UpsertDialect implementation for the current database
     */
    fun getDialect(): UpsertDialect {
        return dataSource.connection.use { connection ->
            when (getDatabaseType(connection)) {
                DatabaseType.POSTGRESQL -> PostgreSqlUpsertDialect()
                DatabaseType.MYSQL -> MySqlUpsertDialect()
                else -> throw UnsupportedOperationException("Unsupported database type")
            }
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
