package io.github.mpecan.upsert.integration

import io.github.mpecan.upsert.dialect.ColumnInfo
import io.github.mpecan.upsert.dialect.PostgreSqlUpsertDialect
import io.github.mpecan.upsert.entity.JpaTestEntityWithConverter
import io.github.mpecan.upsert.entity.JsonData
import io.github.mpecan.upsert.entity.JsonDataConverter
import io.github.mpecan.upsert.model.MockUpsertModelMetadataProvider
import io.github.mpecan.upsert.model.UpsertModel
import io.github.mpecan.upsert.repository.JdbcUpsertOperations
import io.github.mpecan.upsert.repository.UpsertRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.ResultSet
import javax.sql.DataSource

/**
 * Integration tests for upsert operations with custom converters.
 */
@Testcontainers
class ConverterIntegrationTest {

    @Container
    private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:13")
        .apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

    private lateinit var dataSource: DataSource
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    private lateinit var repository: UpsertRepository<JpaTestEntityWithConverter, Long>
    private val jsonDataConverter = JsonDataConverter()

    @BeforeEach
    fun setup() {
        // Create a DataSource connected to the PostgreSQL container
        dataSource = org.springframework.jdbc.datasource.DriverManagerDataSource().apply {
            url = postgresContainer.jdbcUrl
            username = postgresContainer.username
            password = postgresContainer.password
        }

        jdbcTemplate = JdbcTemplate(dataSource)
        namedParameterJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        // Create the test tables
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS jpa_test_entity_with_converter (
                id BIGINT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                json_data TEXT,
                active BOOLEAN NOT NULL
            )
        """
        )

        // Clear the table before each test
        jdbcTemplate.execute("DELETE FROM jpa_test_entity_with_converter")

        // Create repository for JpaTestEntityWithConverter
        val idColumn = ColumnInfo("id", "id", Long::class.java, 4, false)
        val nameColumn = ColumnInfo("name", "name", String::class.java, 12, false)
        val jsonDataColumn = ColumnInfo("json_data", "jsonData", JsonData::class.java, 12, false)
        val activeColumn = ColumnInfo("active", "active", Boolean::class.java, 16, false)

        val metadataProvider = MockUpsertModelMetadataProvider(
            tableName = "jpa_test_entity_with_converter",
            columns = listOf(idColumn, nameColumn, jsonDataColumn, activeColumn),
            idColumn = idColumn,
            entityClass = JpaTestEntityWithConverter::class.java
        )

        val upsertModel = UpsertModel(metadataProvider)
        val dialect = PostgreSqlUpsertDialect()
        val upsertOperations = JdbcUpsertOperations(namedParameterJdbcTemplate, dialect, upsertModel)
        repository =
            io.github.mpecan.upsert.repository.UpsertRepositoryImpl(upsertOperations, upsertModel)
    }

    @Test
    fun `test upsert with custom converter`() {
        // Given
        val entity = JpaTestEntityWithConverter(
            id = 1L,
            name = "Test Entity",
            jsonData = JsonData("test-key", "test-value"),
            active = true
        )

        // When
        val result = repository.upsert(entity)

        // Then
        assertEquals(1L, result.id)
        assertEquals("Test Entity", result.name)
        assertEquals("test-key", result.jsonData.key)
        assertEquals("test-value", result.jsonData.value)
        assertEquals(true, result.active)

        // Verify the data was correctly stored in the database
        val storedEntity = jdbcTemplate.queryForObject(
            "SELECT * FROM jpa_test_entity_with_converter WHERE id = ?",
            entityRowMapper(),
            1L
        )

        assertEquals(1L, storedEntity?.id)
        assertEquals("Test Entity", storedEntity?.name)
        assertEquals("test-key", storedEntity?.jsonData?.key)
        assertEquals("test-value", storedEntity?.jsonData?.value)
        assertEquals(true, storedEntity?.active)
    }

    @Test
    fun `test upsert update with custom converter`() {
        // Given
        // First insert an entity
        val entity1 = JpaTestEntityWithConverter(
            id = 2L,
            name = "Original Entity",
            jsonData = JsonData("original-key", "original-value"),
            active = true
        )
        repository.upsert(entity1)

        // Now create an updated entity with the same ID
        val entity2 = JpaTestEntityWithConverter(
            id = 2L,
            name = "Updated Entity",
            jsonData = JsonData("updated-key", "updated-value"),
            active = false
        )

        // When
        val result = repository.upsert(entity2)

        // Then
        assertEquals(2L, result.id)
        assertEquals("Updated Entity", result.name)
        assertEquals("updated-key", result.jsonData.key)
        assertEquals("updated-value", result.jsonData.value)
        assertEquals(false, result.active)

        // Verify the data was correctly updated in the database
        val storedEntity = jdbcTemplate.queryForObject(
            "SELECT * FROM jpa_test_entity_with_converter WHERE id = ?",
            entityRowMapper(),
            2L
        )

        assertEquals(2L, storedEntity?.id)
        assertEquals("Updated Entity", storedEntity?.name)
        assertEquals("updated-key", storedEntity?.jsonData?.key)
        assertEquals("updated-value", storedEntity?.jsonData?.value)
        assertEquals(false, storedEntity?.active)
    }

    private fun entityRowMapper(): RowMapper<JpaTestEntityWithConverter> {
        return RowMapper { rs: ResultSet, _: Int ->
            val id = rs.getLong("id")
            val name = rs.getString("name")
            val jsonDataStr = rs.getString("json_data")
            val jsonData = jsonDataConverter.convertToEntityAttribute(jsonDataStr)
            val active = rs.getBoolean("active")

            JpaTestEntityWithConverter(id, name, jsonData!!, active)
        }
    }
}
