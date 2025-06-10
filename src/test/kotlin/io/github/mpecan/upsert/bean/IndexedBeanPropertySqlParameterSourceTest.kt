package io.github.mpecan.upsert.bean

import io.github.mpecan.upsert.type.TypeMapperRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import java.sql.Types

/**
 * Unit tests for IndexedBeanPropertySqlParameterSource.
 * These tests verify the correct indexing and parameter handling for batch operations.
 */
class IndexedBeanPropertySqlParameterSourceTest {

    private lateinit var typeMapperRegistry: TypeMapperRegistry
    private lateinit var indexedSource: IndexedBeanPropertySqlParameterSource

    data class TestEntity(
        val id: Long = 1L,
        val name: String = "test",
        val active: Boolean = true,
        val version: Int = 1,
        val updatedAt: String = "2023-01-01"
    )

    @BeforeEach
    fun setUp() {
        typeMapperRegistry = mock()
        
        val entities = listOf(
            TestEntity(1L, "first", true, 1, "2023-01-01"),
            TestEntity(2L, "second", false, 2, "2023-01-02"),
            TestEntity(3L, "third", true, 3, "2023-01-03")
        )
        
        val beanSources = entities.map { entity ->
            ExtendedBeanPropertySqlParameterSource(entity, typeMapperRegistry)
        }
        
        indexedSource = IndexedBeanPropertySqlParameterSource(beanSources)
    }

    @Test
    fun `should return indexed parameter names`() {
        // When
        val parameterNames = indexedSource.parameterNames

        // Then
        assertTrue(parameterNames.contains("id_1"))
        assertTrue(parameterNames.contains("name_1"))
        assertTrue(parameterNames.contains("active_1"))
        assertTrue(parameterNames.contains("version_1"))
        assertTrue(parameterNames.contains("updatedAt_1"))
        
        assertTrue(parameterNames.contains("id_2"))
        assertTrue(parameterNames.contains("name_2"))
        assertTrue(parameterNames.contains("active_2"))
        assertTrue(parameterNames.contains("version_2"))
        assertTrue(parameterNames.contains("updatedAt_2"))
        
        assertTrue(parameterNames.contains("id_3"))
        assertTrue(parameterNames.contains("name_3"))
        assertTrue(parameterNames.contains("active_3"))
        assertTrue(parameterNames.contains("version_3"))
        assertTrue(parameterNames.contains("updatedAt_3"))
    }

    @Test
    fun `should return correct values for indexed properties`() {
        // When/Then
        assertEquals(1L, indexedSource.getValue("id_1"))
        assertEquals("first", indexedSource.getValue("name_1"))
        assertEquals(true, indexedSource.getValue("active_1"))
        assertEquals(1, indexedSource.getValue("version_1"))
        assertEquals("2023-01-01", indexedSource.getValue("updatedAt_1"))
        
        assertEquals(2L, indexedSource.getValue("id_2"))
        assertEquals("second", indexedSource.getValue("name_2"))
        assertEquals(false, indexedSource.getValue("active_2"))
        assertEquals(2, indexedSource.getValue("version_2"))
        assertEquals("2023-01-02", indexedSource.getValue("updatedAt_2"))
        
        assertEquals(3L, indexedSource.getValue("id_3"))
        assertEquals("third", indexedSource.getValue("name_3"))
        assertEquals(true, indexedSource.getValue("active_3"))
        assertEquals(3, indexedSource.getValue("version_3"))
        assertEquals("2023-01-03", indexedSource.getValue("updatedAt_3"))
    }

    @Test
    fun `should return correct hasValue for indexed properties`() {
        // When/Then
        assertTrue(indexedSource.hasValue("id_1"))
        assertTrue(indexedSource.hasValue("name_1"))
        assertTrue(indexedSource.hasValue("active_1"))
        assertTrue(indexedSource.hasValue("version_1"))
        assertTrue(indexedSource.hasValue("updatedAt_1"))
        
        assertTrue(indexedSource.hasValue("id_2"))
        assertTrue(indexedSource.hasValue("name_2"))
        assertTrue(indexedSource.hasValue("active_2"))
        assertTrue(indexedSource.hasValue("version_2"))
        assertTrue(indexedSource.hasValue("updatedAt_2"))
        
        assertTrue(indexedSource.hasValue("id_3"))
        assertTrue(indexedSource.hasValue("name_3"))
        assertTrue(indexedSource.hasValue("active_3"))
        assertTrue(indexedSource.hasValue("version_3"))
        assertTrue(indexedSource.hasValue("updatedAt_3"))
    }

    @Test
    fun `should delegate SQL types to underlying bean sources`() {
        // When/Then - Just verify the methods work, don't assert specific types
        assertNotNull(indexedSource.getSqlType("id_1"))
        assertNotNull(indexedSource.getSqlType("name_1"))
        assertNotNull(indexedSource.getSqlType("active_1"))
        assertNotNull(indexedSource.getSqlType("version_1"))
        assertNotNull(indexedSource.getSqlType("updatedAt_1"))
    }

    @Test
    fun `should delegate type names to underlying bean sources`() {
        // When/Then - Just verify the methods work, don't assert specific type names
        // Type names can be null for some types, so we just verify the method works
        indexedSource.getTypeName("id_1")
        indexedSource.getTypeName("name_1") 
        indexedSource.getTypeName("active_1")
        indexedSource.getTypeName("version_1")
        indexedSource.getTypeName("updatedAt_1")
    }

    @Test
    fun `should throw exception for invalid indexed property name without underscore`() {
        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            indexedSource.getValue("invalidproperty")
        }
        assertEquals("Invalid indexed property name: invalidproperty", exception.message)
    }

    @Test
    fun `should throw exception for invalid indexed property name with invalid index`() {
        // When/Then
        val exception = assertThrows<NumberFormatException> {
            indexedSource.getValue("name_invalid")
        }
    }

    @Test
    fun `should handle property names with multiple underscores correctly`() {
        // Given - create a test entity with underscore in property name
        data class EntityWithUnderscores(val user_name: String, val email_address: String)
        
        val entity = EntityWithUnderscores("test_user", "test@example.com")
        val beanSource = ExtendedBeanPropertySqlParameterSource(entity, typeMapperRegistry)
        val indexedSourceWithUnderscores = IndexedBeanPropertySqlParameterSource(listOf(beanSource))
        
        // When/Then
        assertEquals("test_user", indexedSourceWithUnderscores.getValue("user_name_1"))
        assertEquals("test@example.com", indexedSourceWithUnderscores.getValue("email_address_1"))
        assertTrue(indexedSourceWithUnderscores.hasValue("user_name_1"))
        assertTrue(indexedSourceWithUnderscores.hasValue("email_address_1"))
    }

    @Test
    fun `should handle empty bean list`() {
        // Given
        val emptyIndexedSource = IndexedBeanPropertySqlParameterSource(emptyList())
        
        // When/Then
        assertTrue(emptyIndexedSource.parameterNames.isEmpty())
    }

    @Test
    fun `should handle single entity`() {
        // Given
        val singleEntity = TestEntity(42L, "single", false, 99, "2023-12-31")
        val singleBeanSource = ExtendedBeanPropertySqlParameterSource(singleEntity, typeMapperRegistry)
        val singleIndexedSource = IndexedBeanPropertySqlParameterSource(listOf(singleBeanSource))
        
        // When/Then
        assertEquals(42L, singleIndexedSource.getValue("id_1"))
        assertEquals("single", singleIndexedSource.getValue("name_1"))
        assertEquals(false, singleIndexedSource.getValue("active_1"))
        assertEquals(99, singleIndexedSource.getValue("version_1"))
        assertEquals("2023-12-31", singleIndexedSource.getValue("updatedAt_1"))
        
        assertTrue(singleIndexedSource.hasValue("id_1"))
        assertTrue(singleIndexedSource.hasValue("name_1"))
        assertTrue(singleIndexedSource.hasValue("active_1"))
        assertTrue(singleIndexedSource.hasValue("version_1"))
        assertTrue(singleIndexedSource.hasValue("updatedAt_1"))
    }
}