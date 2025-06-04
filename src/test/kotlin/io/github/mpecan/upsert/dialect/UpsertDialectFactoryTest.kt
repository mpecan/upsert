package io.github.mpecan.upsert.dialect

import io.github.mpecan.upsert.type.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.sql.Connection
import java.sql.DatabaseMetaData
import javax.sql.DataSource

/**
 * Unit tests for UpsertDialectFactory.
 */
class UpsertDialectFactoryTest {

    private val typeMapperRegistry = TypeMapperRegistry(testTypeProvider())

    @Test
    fun `should return PostgreSqlUpsertDialect for PostgreSQL database`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val connection = mock(Connection::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(dataSource.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(metadata)
        `when`(metadata.databaseProductName).thenReturn("PostgreSQL")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val dialect = factory.getDialect()

        // Then
        assertTrue(dialect is PostgreSqlUpsertDialect)
    }

    @Test
    fun `should return MySqlUpsertDialect for MySQL 8_0_19 and later`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val connection = mock(Connection::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(dataSource.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(metadata)
        `when`(metadata.databaseProductName).thenReturn("MySQL")
        `when`(metadata.databaseMajorVersion).thenReturn(8)
        `when`(metadata.databaseMinorVersion).thenReturn(0)
        `when`(metadata.databaseProductVersion).thenReturn("8.0.19")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val dialect = factory.getDialect()

        // Then
        assertTrue(dialect is MySqlUpsertDialect)
    }

    @Test
    fun `should return MySqlLegacyUpsertDialect for MySQL before 8_0_19`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val connection = mock(Connection::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(dataSource.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(metadata)
        `when`(metadata.databaseProductName).thenReturn("MySQL")
        `when`(metadata.databaseMajorVersion).thenReturn(8)
        `when`(metadata.databaseMinorVersion).thenReturn(0)
        `when`(metadata.databaseProductVersion).thenReturn("8.0.18")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val dialect = factory.getDialect()

        // Then
        assertTrue(dialect is MySqlLegacyUpsertDialect)
    }

    @Test
    fun `should return MySqlUpsertDialect for MySQL 9_0_0`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val connection = mock(Connection::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(dataSource.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(metadata)
        `when`(metadata.databaseProductName).thenReturn("MySQL")
        `when`(metadata.databaseMajorVersion).thenReturn(9)
        `when`(metadata.databaseMinorVersion).thenReturn(0)
        `when`(metadata.databaseProductVersion).thenReturn("9.0.0")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val dialect = factory.getDialect()

        // Then
        assertTrue(dialect is MySqlUpsertDialect)
    }

    @Test
    fun `should return MySqlUpsertDialect for MySQL 8_1_0`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val connection = mock(Connection::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(dataSource.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(metadata)
        `when`(metadata.databaseProductName).thenReturn("MySQL")
        `when`(metadata.databaseMajorVersion).thenReturn(8)
        `when`(metadata.databaseMinorVersion).thenReturn(1)
        `when`(metadata.databaseProductVersion).thenReturn("8.1.0")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val dialect = factory.getDialect()

        // Then
        assertTrue(dialect is MySqlUpsertDialect)
    }

    @Test
    fun `should return MySqlLegacyUpsertDialect for MySQL 8_0_12`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val connection = mock(Connection::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(dataSource.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(metadata)
        `when`(metadata.databaseProductName).thenReturn("MySQL")
        `when`(metadata.databaseMajorVersion).thenReturn(8)
        `when`(metadata.databaseMinorVersion).thenReturn(0)
        `when`(metadata.databaseProductVersion).thenReturn("8.0.12")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val dialect = factory.getDialect()

        // Then
        assertTrue(dialect is MySqlLegacyUpsertDialect)
    }

    @Test
    fun `should return MySqlLegacyUpsertDialect for MySQL 5_7_0`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val connection = mock(Connection::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(dataSource.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(metadata)
        `when`(metadata.databaseProductName).thenReturn("MySQL")
        `when`(metadata.databaseMajorVersion).thenReturn(5)
        `when`(metadata.databaseMinorVersion).thenReturn(7)
        `when`(metadata.databaseProductVersion).thenReturn("5.7.0")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val dialect = factory.getDialect()

        // Then
        assertTrue(dialect is MySqlLegacyUpsertDialect)
    }

    @Test
    fun `should handle version parsing error gracefully and default to modern dialect`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val connection = mock(Connection::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(dataSource.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(metadata)
        `when`(metadata.databaseProductName).thenReturn("MySQL")
        `when`(metadata.databaseMajorVersion).thenReturn(8)
        `when`(metadata.databaseMinorVersion).thenReturn(0)
        `when`(metadata.databaseProductVersion).thenReturn("8.0.invalid-version")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val dialect = factory.getDialect()

        // Then
        assertTrue(dialect is MySqlUpsertDialect)
    }

    @Test
    fun `should throw UnsupportedOperationException for unknown database type`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val connection = mock(Connection::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(dataSource.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(metadata)
        `when`(metadata.databaseProductName).thenReturn("Oracle")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When & Then
        assertThrows(UnsupportedOperationException::class.java) {
            factory.getDialect()
        }
    }

    @Test
    fun `checkIfModernMySQLVersion should return true for version 8_0_19`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(metadata.databaseMajorVersion).thenReturn(8)
        `when`(metadata.databaseMinorVersion).thenReturn(0)
        `when`(metadata.databaseProductVersion).thenReturn("8.0.19")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val result = factory.checkIfModernMySQLVersion(metadata)

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkIfModernMySQLVersion should return false for version 8_0_18`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(metadata.databaseMajorVersion).thenReturn(8)
        `when`(metadata.databaseMinorVersion).thenReturn(0)
        `when`(metadata.databaseProductVersion).thenReturn("8.0.18")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val result = factory.checkIfModernMySQLVersion(metadata)

        // Then
        assertFalse(result)
    }

    @Test
    fun `checkIfModernMySQLVersion should return true for version 8_0_20`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(metadata.databaseMajorVersion).thenReturn(8)
        `when`(metadata.databaseMinorVersion).thenReturn(0)
        `when`(metadata.databaseProductVersion).thenReturn("8.0.20")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val result = factory.checkIfModernMySQLVersion(metadata)

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkIfModernMySQLVersion should return true for version 9_0_0`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(metadata.databaseMajorVersion).thenReturn(9)
        `when`(metadata.databaseMinorVersion).thenReturn(0)
        `when`(metadata.databaseProductVersion).thenReturn("9.0.0")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val result = factory.checkIfModernMySQLVersion(metadata)

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkIfModernMySQLVersion should return false for version 5_7_35`() {
        // Given
        val dataSource = mock(DataSource::class.java)
        val metadata = mock(DatabaseMetaData::class.java)
        
        `when`(metadata.databaseMajorVersion).thenReturn(5)
        `when`(metadata.databaseMinorVersion).thenReturn(7)
        `when`(metadata.databaseProductVersion).thenReturn("5.7.35")
        
        val factory = UpsertDialectFactory(dataSource, typeMapperRegistry)

        // When
        val result = factory.checkIfModernMySQLVersion(metadata)

        // Then
        assertFalse(result)
    }
}