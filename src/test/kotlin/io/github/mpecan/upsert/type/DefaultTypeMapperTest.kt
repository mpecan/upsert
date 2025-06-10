package io.github.mpecan.upsert.type

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.lang.reflect.Field
import java.sql.Types

class DefaultTypeMapperTest {

    private val defaultTypeMapper = DefaultTypeMapper()

    @Test
    fun `should handle any field`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        
        assertTrue(defaultTypeMapper.canHandle(field))
    }

    @Test
    fun `should handle any value`() {
        assertTrue(defaultTypeMapper.canHandleValue("test"))
        assertTrue(defaultTypeMapper.canHandleValue(123))
        assertTrue(defaultTypeMapper.canHandleValue(null))
        assertTrue(defaultTypeMapper.canHandleValue(TestEnum.VALUE1))
    }

    @Test
    fun `should return null for null value`() {
        val result = defaultTypeMapper.convertToJdbcValue(null)
        
        assertNull(result)
    }

    @Test
    fun `should convert enum to name string`() {
        val result = defaultTypeMapper.convertToJdbcValue(TestEnum.VALUE1)
        
        assertEquals("VALUE1", result)
    }

    @Test
    fun `should return value unchanged for non-enum types`() {
        val stringValue = "test"
        val intValue = 42
        val booleanValue = true
        
        assertEquals(stringValue, defaultTypeMapper.convertToJdbcValue(stringValue))
        assertEquals(intValue, defaultTypeMapper.convertToJdbcValue(intValue))
        assertEquals(booleanValue, defaultTypeMapper.convertToJdbcValue(booleanValue))
    }

    @Test
    fun `should get SQL type for field using TypeMapper static method`() {
        val stringField = TestEntity::class.java.getDeclaredField("stringField")
        val intField = TestEntity::class.java.getDeclaredField("intField")
        
        assertEquals(Types.VARCHAR, defaultTypeMapper.getSqlTypeForField(stringField))
        assertEquals(Types.INTEGER, defaultTypeMapper.getSqlTypeForField(intField))
    }

    // Test enum
    enum class TestEnum {
        VALUE1, VALUE2
    }

    // Test entity class
    class TestEntity {
        var stringField: String = ""
        var intField: Int = 0
    }
}