package io.github.mpecan.upsert.model

import io.github.mpecan.upsert.dialect.ColumnInfo
import io.github.mpecan.upsert.entity.JpaTestEntity
import io.github.mpecan.upsert.entity.JpaTestEntityWithGeneratedId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for UpsertModel.
 * These tests verify that UpsertModel correctly validates and processes entities for upsert operations.
 *
 * Note: We're using MockUpsertModelMetadataProvider, which allows us to test UpsertModel without relying on JPA metadata.
 */
class UpsertModelTest {

    private lateinit var upsertModel: UpsertModel
    private lateinit var upsertModelWithGeneratedId: UpsertModel

    private val idColumn = ColumnInfo("id", "id", Long::class.java, 4, false)
    private val nameColumn = ColumnInfo("name", "name", String::class.java, 12, false)
    private val descriptionColumn =
        ColumnInfo("description", "description", String::class.java, 12, false)
    private val activeColumn = ColumnInfo("active", "active", Boolean::class.java, 16, false)
    private val generatedIdColumn = ColumnInfo("id", "id", Long::class.java, 4, true)

    @BeforeEach
    fun setUp() {
        // Set up UpsertModel for JpaTestEntity
        val metadataProvider = MockUpsertModelMetadataProvider(
            tableName = "jpa_test_entity",
            columns = listOf(idColumn, nameColumn, descriptionColumn, activeColumn),
            idColumn = idColumn,
            entityClass = JpaTestEntity::class.java
        )
        upsertModel = UpsertModel(metadataProvider)

        // Set up UpsertModel for JpaTestEntityWithGeneratedId
        val metadataProviderWithGeneratedId = MockUpsertModelMetadataProvider(
            tableName = "jpa_test_entity_with_generated_id",
            columns = listOf(generatedIdColumn, nameColumn, descriptionColumn, activeColumn),
            idColumn = generatedIdColumn,
            uniqueColumns = listOf(nameColumn), // JpaTestEntityWithGeneratedId has a unique constraint on name
            entityClass = JpaTestEntityWithGeneratedId::class.java
        )
        upsertModelWithGeneratedId = UpsertModel(metadataProviderWithGeneratedId)
    }

    /**
     * Test that UpsertModel correctly identifies values that will be part of the insert query.
     * This tests the getDefaultValues() method.
     */
    @Test
    fun `should identify values for insert query`() {
        // When
        val values = upsertModel.getDefaultValues()

        // Then
        assertEquals(4, values.size)
        assertTrue(values.any { it.name == "id" })
        assertTrue(values.any { it.name == "name" })
        assertTrue(values.any { it.name == "description" })
        assertTrue(values.any { it.name == "active" })
    }

    /**
     * Test that UpsertModel correctly identifies fields that will be used to define the exclusive relationship.
     * This tests the getDefaultOnColumns() method.
     */
    @Test
    fun `should identify fields for exclusive relationship`() {
        // When
        val onColumns = upsertModel.getDefaultOnColumns()

        // Then
        assertEquals(1, onColumns.size)
        assertEquals("id", onColumns[0].name)
    }

    /**
     * Test that UpsertModel correctly identifies fields that will not be overwritten on conflict.
     * This tests the getDefaultUpdateColumns() method.
     */
    @Test
    fun `should identify fields that will not be overwritten on conflict`() {
        // When
        val updateColumns = upsertModel.getDefaultUpdateColumns()

        // Then
        assertEquals(3, updateColumns.size)
        assertTrue(updateColumns.any { it.name == "name" })
        assertTrue(updateColumns.any { it.name == "description" })
        assertTrue(updateColumns.any { it.name == "active" })
        assertFalse(updateColumns.any { it.name == "id" })
    }

    /**
     * Test that UpsertModel correctly validates an upsert query.
     * This tests the validateUpsertQuery() method.
     */
    @Test
    fun `should validate upsert query`() {
        // Given
        val values = listOf(idColumn, nameColumn, descriptionColumn, activeColumn)
        val onColumns = listOf(idColumn)
        val updateColumns = listOf(nameColumn, descriptionColumn, activeColumn)

        // When/Then
        assertDoesNotThrow {
            upsertModel.validateUpsertQuery(values, onColumns, updateColumns)
        }
    }

    /**
     * Test that UpsertModel throws an exception when validating an upsert query with invalid columns.
     * This tests the validateUpsertQuery() method with invalid input.
     */
    @Test
    fun `should throw exception when validating upsert query with invalid columns`() {
        // Given
        val invalidColumn =
            ColumnInfo("invalid", "invalid", String::class.java, 12, false) // Invalid column
        val values = listOf(idColumn, nameColumn, descriptionColumn, activeColumn, invalidColumn)
        val onColumns = listOf(idColumn)
        val updateColumns = listOf(nameColumn, descriptionColumn, activeColumn)

        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            upsertModel.validateUpsertQuery(values, onColumns, updateColumns)
        }
        assertTrue(exception.message!!.contains("invalid"))
    }

    /**
     * Test that UpsertModel throws an exception when validating an upsert query with invalid on columns.
     * This tests the validateUpsertQuery() method with invalid input.
     */
    @Test
    fun `should throw exception when validating upsert query with invalid on columns`() {
        // Given
        val values = listOf(idColumn, nameColumn, descriptionColumn, activeColumn)
        val onColumns = listOf(nameColumn) // Not a primary key or unique constraint in upsertModel
        val updateColumns = listOf(descriptionColumn, activeColumn)

        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            upsertModel.validateUpsertQuery(values, onColumns, updateColumns)
        }
        assertTrue(exception.message!!.contains("name"))
    }

    /**
     * Test that UpsertModel throws an exception when validating an upsert query with update columns that are part of the ON clause.
     * This tests the validateUpsertQuery() method with invalid input.
     */
    @Test
    fun `should throw exception when validating upsert query with update columns that are part of the ON clause`() {
        // Given
        val values = listOf(idColumn, nameColumn, descriptionColumn, activeColumn)
        val onColumns = listOf(idColumn)
        val updateColumns = listOf(
            idColumn,
            nameColumn,
            descriptionColumn,
            activeColumn
        ) // idColumn is part of the ON clause

        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            upsertModel.validateUpsertQuery(values, onColumns, updateColumns)
        }
        assertTrue(exception.message!!.contains("id"))
    }

    /**
     * Test that UpsertModel correctly creates an UpsertInstance.
     * This tests the createUpsertInstance() method.
     */
    @Test
    fun `should create upsert instance`() {
        // Given
        val tableName = "jpa_test_entity"
        val onColumns = listOf("id")
        val values = listOf("name", "description", "active")

        // When
        val upsertInstance = upsertModel.createUpsertInstance(onColumns, values, emptyList())

        // Then
        assertEquals(tableName, upsertInstance.tableName)
        assertEquals(1, upsertInstance.onColumns.size)
        assertEquals("id", upsertInstance.onColumns[0].name)
        assertEquals(3, upsertInstance.values.size)
        assertTrue(upsertInstance.values.any { column -> column.name == "name" })
        assertTrue(upsertInstance.values.any { column -> column.name == "description" })
        assertTrue(upsertInstance.values.any { column -> column.name == "active" })
        assertEquals(3, upsertInstance.updateColumns.size)
        assertTrue(upsertInstance.updateColumns.any { column -> column.name == "name" })
        assertTrue(upsertInstance.updateColumns.any { column -> column.name == "description" })
        assertTrue(upsertInstance.updateColumns.any { column -> column.name == "active" })
    }

    /**
     * Test that UpsertModel correctly handles entities with generated IDs.
     * This tests the behavior with JpaTestEntityWithGeneratedId.
     */
    @Test
    fun `should handle entities with generated IDs`() {
        // When
        val values = upsertModelWithGeneratedId.getDefaultValues()
        val onColumns = upsertModelWithGeneratedId.getDefaultOnColumns()
        val updateColumns = upsertModelWithGeneratedId.getDefaultUpdateColumns()

        // Then
        assertTrue(values.any { column -> column.name == "name" })
        assertTrue(values.any { column -> column.name == "description" })
        assertTrue(values.any { column -> column.name == "active" })
        assertTrue(onColumns.any { column -> column.name == "id" })
        assertTrue(updateColumns.any { column -> column.name == "name" })
        assertTrue(updateColumns.any { column -> column.name == "description" })
        assertTrue(updateColumns.any { column -> column.name == "active" })
        assertFalse(updateColumns.any { column -> column.name == "id" })
    }
}
