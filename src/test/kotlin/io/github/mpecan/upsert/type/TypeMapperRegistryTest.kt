package io.github.mpecan.upsert.type

import org.hibernate.annotations.JdbcTypeCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.*
import org.springframework.beans.factory.ObjectProvider
import java.lang.reflect.Field
import java.sql.Types

class TypeMapperRegistryTest {

    private lateinit var mockMapperProvider: ObjectProvider<List<TypeMapper>>
    private lateinit var typeMapperRegistry: TypeMapperRegistry
    private lateinit var mockMapper1: TypeMapper
    private lateinit var mockMapper2: TypeMapper

    @BeforeEach
    fun setUp() {
        mockMapperProvider = mock(ObjectProvider::class.java) as ObjectProvider<List<TypeMapper>>
        mockMapper1 = mock(TypeMapper::class.java)
        mockMapper2 = mock(TypeMapper::class.java)
        
        `when`(mockMapperProvider.getObject()).thenReturn(listOf(mockMapper1, mockMapper2))
        typeMapperRegistry = TypeMapperRegistry(mockMapperProvider)
    }

    @Test
    fun `should return null when converting null value`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        
        val result = typeMapperRegistry.convertToJdbcValue(null, field)
        
        assertNull(result)
    }

    @Test
    fun `should convert value using appropriate mapper`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        val inputValue = "test"
        val expectedValue = "converted_test"
        
        `when`(mockMapper1.canHandle(field)).thenReturn(true)
        `when`(mockMapper1.convertToJdbcValue(inputValue)).thenReturn(expectedValue)
        
        val result = typeMapperRegistry.convertToJdbcValue(inputValue, field)
        
        assertEquals(expectedValue, result)
        verify(mockMapper1).canHandle(field)
        verify(mockMapper1).convertToJdbcValue(inputValue)
    }

    @Test
    fun `should return original value when conversion fails`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        val inputValue = "test"
        
        `when`(mockMapper1.canHandle(field)).thenReturn(true)
        `when`(mockMapper1.convertToJdbcValue(inputValue)).thenThrow(RuntimeException("Conversion failed"))
        
        val result = typeMapperRegistry.convertToJdbcValue(inputValue, field)
        
        assertEquals(inputValue, result)
    }

    @Test
    fun `should use DefaultTypeMapper when no mapper can handle field`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        val inputValue = "test"
        
        `when`(mockMapper1.canHandle(field)).thenReturn(false)
        `when`(mockMapper2.canHandle(field)).thenReturn(false)
        
        val result = typeMapperRegistry.convertToJdbcValue(inputValue, field)
        
        // DefaultTypeMapper should return the original value for String
        assertEquals(inputValue, result)
    }

    @Test
    fun `should handle exception when checking if mapper can handle field`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        val inputValue = "test"
        
        `when`(mockMapper1.canHandle(field)).thenThrow(RuntimeException("Handler check failed"))
        `when`(mockMapper2.canHandle(field)).thenReturn(true)
        `when`(mockMapper2.convertToJdbcValue(inputValue)).thenReturn("converted")
        
        val result = typeMapperRegistry.convertToJdbcValue(inputValue, field)
        
        assertEquals("converted", result)
        verify(mockMapper1).canHandle(field)
        verify(mockMapper2).canHandle(field)
    }

    @Test
    fun `should get SQL type from mapper when annotation not found`() {
        val field = TestEntity::class.java.getDeclaredField("annotatedField")
        val expectedSqlType = Types.LONGVARCHAR
        
        `when`(mockMapper1.canHandle(field)).thenReturn(true)
        `when`(mockMapper1.getSqlTypeForField(field)).thenReturn(expectedSqlType)
        
        val result = typeMapperRegistry.getSqlTypeForField(field)
        
        assertEquals(expectedSqlType, result)
        verify(mockMapper1).getSqlTypeForField(field)
    }

    @Test
    fun `should get SQL type from mapper when no annotation present`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        val expectedSqlType = Types.LONGVARCHAR
        
        `when`(mockMapper1.canHandle(field)).thenReturn(true)
        `when`(mockMapper1.getSqlTypeForField(field)).thenReturn(expectedSqlType)
        
        val result = typeMapperRegistry.getSqlTypeForField(field)
        
        assertEquals(expectedSqlType, result)
        verify(mockMapper1).getSqlTypeForField(field)
    }

    @Test
    fun `should fallback to default SQL type when mapper throws exception`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        
        `when`(mockMapper1.canHandle(field)).thenReturn(true)
        `when`(mockMapper1.getSqlTypeForField(field)).thenThrow(RuntimeException("SQL type error"))
        
        val result = typeMapperRegistry.getSqlTypeForField(field)
        
        // Should fall back to TypeMapper.getSqlType for String
        assertEquals(Types.VARCHAR, result)
    }

    @Test
    fun `should fallback to default type when mapper selection fails`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        
        `when`(mockMapper1.canHandle(field)).thenThrow(RuntimeException("Fatal error"))
        `when`(mockMapper2.canHandle(field)).thenThrow(RuntimeException("Fatal error"))
        
        val result = typeMapperRegistry.getSqlTypeForField(field)
        
        // Should fallback to DefaultTypeMapper which uses TypeMapper.getSqlType
        assertEquals(Types.VARCHAR, result)
    }

    @Test
    fun `should return OTHER type when type determination throws exception`() {
        // Create a registry that will throw an exception when accessing annotations
        val badMapperProvider = mock(ObjectProvider::class.java) as ObjectProvider<List<TypeMapper>>
        `when`(badMapperProvider.getObject()).thenThrow(RuntimeException("Provider failure"))
        val badRegistry = TypeMapperRegistry(badMapperProvider)
        
        val field = TestEntity::class.java.getDeclaredField("stringField")
        
        val result = badRegistry.getSqlTypeForField(field)
        
        assertEquals(Types.OTHER, result)
    }

    @Test
    fun `should return list of registered mappers`() {
        val result = typeMapperRegistry.getRegisteredMappers()
        
        assertEquals(2, result.size)
        assertTrue(result.contains(mockMapper1))
        assertTrue(result.contains(mockMapper2))
    }

    @Test
    fun `should use second mapper when first cannot handle field`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        val inputValue = "test"
        val expectedValue = "converted_by_second"
        
        `when`(mockMapper1.canHandle(field)).thenReturn(false)
        `when`(mockMapper2.canHandle(field)).thenReturn(true)
        `when`(mockMapper2.convertToJdbcValue(inputValue)).thenReturn(expectedValue)
        
        val result = typeMapperRegistry.convertToJdbcValue(inputValue, field)
        
        assertEquals(expectedValue, result)
        verify(mockMapper1).canHandle(field)
        verify(mockMapper2).canHandle(field)
        verify(mockMapper2).convertToJdbcValue(inputValue)
    }

    // Test entity class for reflection
    class TestEntity {
        var stringField: String = ""
        
        @JdbcTypeCode(2016) // Types.JSON
        var annotatedField: String = ""
    }
}