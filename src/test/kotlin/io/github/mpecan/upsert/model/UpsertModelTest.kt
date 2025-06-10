package io.github.mpecan.upsert.model

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
            uniqueConstraints = listOf(listOf(nameColumn)), // Properly define unique constraints
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

    /**
     * Test getValueColumns with specific fields.
     */
    @Test
    fun `should return specific value columns when fields are provided`() {
        // Given
        val fields = listOf("name", "active")

        // When
        val valueColumns = upsertModel.getValueColumns(fields)

        // Then
        assertEquals(2, valueColumns.size)
        assertTrue(valueColumns.any { it.name == "name" })
        assertTrue(valueColumns.any { it.name == "active" })
        assertFalse(valueColumns.any { it.name == "id" })
        assertFalse(valueColumns.any { it.name == "description" })
    }

    /**
     * Test getValueColumns with empty fields returns default values.
     */
    @Test
    fun `should return default values when fields list is empty`() {
        // Given
        val fields = emptyList<String>()

        // When
        val valueColumns = upsertModel.getValueColumns(fields)

        // Then
        assertEquals(4, valueColumns.size)
        assertTrue(valueColumns.any { it.name == "id" })
        assertTrue(valueColumns.any { it.name == "name" })
        assertTrue(valueColumns.any { it.name == "description" })
        assertTrue(valueColumns.any { it.name == "active" })
    }

    /**
     * Test getOnColumns with specific fields.
     */
    @Test
    fun `should return specific on columns when fields are provided`() {
        // Given - for this test, we'll use name as an ON field (though normally it wouldn't be unique)
        val fields = listOf("id")

        // When
        val onColumns = upsertModel.getOnColumns(fields)

        // Then
        assertEquals(1, onColumns.size)
        assertTrue(onColumns.any { it.name == "id" })
    }

    /**
     * Test getOnColumns with empty fields returns default on columns.
     */
    @Test
    fun `should return default on columns when fields list is empty`() {
        // Given
        val fields = emptyList<String>()

        // When
        val onColumns = upsertModel.getOnColumns(fields)

        // Then
        assertEquals(1, onColumns.size)
        assertTrue(onColumns.any { it.name == "id" })
    }

    /**
     * Test getTableName returns correct table name.
     */
    @Test
    fun `should return correct table name`() {
        // When
        val tableName = upsertModel.getTableName()

        // Then
        assertEquals("jpa_test_entity", tableName)
    }

    /**
     * Test createUpsertInstance with ignoreAllFields set to true.
     */
    @Test
    fun `should create upsert instance with ignore all fields`() {
        // When
        val upsertInstance = upsertModel.createUpsertInstance(
            ignoreAllFields = true
        )

        // Then
        assertEquals("jpa_test_entity", upsertInstance.tableName)
        assertEquals(1, upsertInstance.onColumns.size)
        assertEquals("id", upsertInstance.onColumns[0].name)
        assertEquals(4, upsertInstance.values.size) // All values still included
        assertEquals(0, upsertInstance.updateColumns.size) // No update columns when ignoring all
    }

    /**
     * Test createUpsertInstance with specific ignore columns.
     */
    @Test
    fun `should create upsert instance with specific ignore columns`() {
        // When
        val upsertInstance = upsertModel.createUpsertInstance(
            ignoreColumns = listOf("description", "active")
        )

        // Then
        assertEquals("jpa_test_entity", upsertInstance.tableName)
        assertEquals(1, upsertInstance.onColumns.size)
        assertEquals("id", upsertInstance.onColumns[0].name)
        assertEquals(4, upsertInstance.values.size) // All values still included
        assertEquals(1, upsertInstance.updateColumns.size) // Only name should be updated
        assertTrue(upsertInstance.updateColumns.any { it.name == "name" })
        assertFalse(upsertInstance.updateColumns.any { it.name == "description" })
        assertFalse(upsertInstance.updateColumns.any { it.name == "active" })
        assertFalse(upsertInstance.updateColumns.any { it.name == "id" })
    }

    /**
     * Test createUpsertInstance with empty ignore columns list.
     */
    @Test
    fun `should create upsert instance with empty ignore columns list`() {
        // When
        val upsertInstance = upsertModel.createUpsertInstance(
            ignoreColumns = emptyList()
        )

        // Then
        assertEquals("jpa_test_entity", upsertInstance.tableName)
        assertEquals(1, upsertInstance.onColumns.size)
        assertEquals("id", upsertInstance.onColumns[0].name)
        assertEquals(4, upsertInstance.values.size)
        assertEquals(3, upsertInstance.updateColumns.size) // All non-id columns should be updated
        assertTrue(upsertInstance.updateColumns.any { it.name == "name" })
        assertTrue(upsertInstance.updateColumns.any { it.name == "description" })
        assertTrue(upsertInstance.updateColumns.any { it.name == "active" })
    }

    /**
     * Test createUpsertInstance with conditional info.
     */
    @Test
    fun `should create upsert instance with conditional info`() {
        // Given
        val conditionalInfo = ConditionalInfo(
            fieldName = "version",
            operator = ComparisonOperator.MORE_OR_EQUAL
        )

        // When
        val upsertInstance = upsertModel.createUpsertInstance(
            conditionalInfo = conditionalInfo
        )

        // Then
        assertEquals("jpa_test_entity", upsertInstance.tableName)
        assertEquals(conditionalInfo, upsertInstance.conditionalInfo)
    }

    /**
     * Test UpsertInstance withoutValueColumns method.
     */
    @Test
    fun `should create upsert instance without specific value columns`() {
        // Given
        val originalInstance = upsertModel.createUpsertInstance()
        val columnsToExclude = listOf(nameColumn, activeColumn)

        // When
        val newInstance = originalInstance.withoutValueColumns(columnsToExclude)

        // Then
        assertEquals(originalInstance.tableName, newInstance.tableName)
        assertEquals(originalInstance.onColumns, newInstance.onColumns)
        assertEquals(originalInstance.updateColumns, newInstance.updateColumns)
        assertEquals(originalInstance.conditionalInfo, newInstance.conditionalInfo)
        
        assertEquals(2, newInstance.values.size) // Should have id and description only
        assertTrue(newInstance.values.any { it.name == "id" })
        assertTrue(newInstance.values.any { it.name == "description" })
        assertFalse(newInstance.values.any { it.name == "name" })
        assertFalse(newInstance.values.any { it.name == "active" })
    }

    /**
     * Test UpsertInstance forFirstUniqueConstraint method.
     */
    @Test
    fun `should create upsert instance for first unique constraint`() {
        // When
        val originalInstance = upsertModelWithGeneratedId.createUpsertInstance()
        val uniqueConstraintInstance = originalInstance.forFirstUniqueConstraint()

        // Then
        assertEquals(originalInstance.tableName, uniqueConstraintInstance.tableName)
        assertEquals(originalInstance.values, uniqueConstraintInstance.values)
        assertEquals(originalInstance.conditionalInfo, uniqueConstraintInstance.conditionalInfo)
        
        // Should use unique constraint (name) as ON columns
        assertEquals(1, uniqueConstraintInstance.onColumns.size)
        assertEquals("name", uniqueConstraintInstance.onColumns[0].name)
        
        // Update columns should exclude the unique constraint columns
        assertFalse(uniqueConstraintInstance.updateColumns.any { it.name == "name" })
        assertTrue(uniqueConstraintInstance.updateColumns.any { it.name == "description" })
        assertTrue(uniqueConstraintInstance.updateColumns.any { it.name == "active" })
    }

    /**
     * Test UpsertInstance forFirstUniqueConstraint throws exception when no unique constraints exist.
     */
    @Test
    fun `should throw exception when no unique constraints found for forFirstUniqueConstraint`() {
        // Given - upsertModel (not the WithGeneratedId version) has no unique constraints
        val originalInstance = upsertModel.createUpsertInstance()

        // When/Then
        val exception = assertThrows(IllegalStateException::class.java) {
            originalInstance.forFirstUniqueConstraint()
        }
        assertEquals("No unique constraints found", exception.message)
    }

    /**
     * Test validation with missing ON columns.
     */
    @Test
    fun `should throw exception when validating with missing ON columns`() {
        // Given
        val invalidColumn = ColumnInfo("missing", "missing", String::class.java, 12, false)
        val values = listOf(idColumn, nameColumn)
        val onColumns = listOf(invalidColumn) // Column not present in entity
        val updateColumns = listOf(nameColumn)

        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            upsertModel.validateUpsertQuery(values, onColumns, updateColumns)
        }
        assertTrue(exception.message!!.contains("missing"))
    }

    /**
     * Test validation with missing update columns.
     */
    @Test
    fun `should throw exception when validating with missing update columns`() {
        // Given
        val invalidColumn = ColumnInfo("missing", "missing", String::class.java, 12, false)
        val values = listOf(idColumn, nameColumn)
        val onColumns = listOf(idColumn)
        val updateColumns = listOf(invalidColumn) // Column not present in entity

        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            upsertModel.validateUpsertQuery(values, onColumns, updateColumns)
        }
        assertTrue(exception.message!!.contains("missing"))
    }
}
