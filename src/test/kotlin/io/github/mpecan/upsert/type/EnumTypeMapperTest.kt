package io.github.mpecan.upsert.type

import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.lang.reflect.Field
import java.sql.Types

class EnumTypeMapperTest {

    private val enumTypeMapper = EnumTypeMapper()

    @Test
    fun `should return null for null value`() {
        val result = enumTypeMapper.convertToJdbcValue(null)
        
        assertNull(result)
    }

    @Test
    fun `should convert enum to ordinal value`() {
        val result = enumTypeMapper.convertToJdbcValue(TestEnum.SECOND)
        
        assertEquals(1, result) // SECOND is at index 1
    }

    @Test
    fun `should convert first enum value to ordinal zero`() {
        val result = enumTypeMapper.convertToJdbcValue(TestEnum.FIRST)
        
        assertEquals(0, result) // FIRST is at index 0
    }

    @Test
    fun `should convert third enum value to ordinal two`() {
        val result = enumTypeMapper.convertToJdbcValue(TestEnum.THIRD)
        
        assertEquals(2, result) // THIRD is at index 2
    }

    @Test
    fun `should return original value for non-enum types`() {
        val stringValue = "test"
        val intValue = 42
        val booleanValue = true
        
        assertEquals(stringValue, enumTypeMapper.convertToJdbcValue(stringValue))
        assertEquals(intValue, enumTypeMapper.convertToJdbcValue(intValue))
        assertEquals(booleanValue, enumTypeMapper.convertToJdbcValue(booleanValue))
    }

    @Test
    fun `should handle enum field without annotation`() {
        val field = TestEntity::class.java.getDeclaredField("plainEnumField")
        
        assertTrue(enumTypeMapper.canHandle(field))
    }

    @Test
    fun `should handle enum field with ORDINAL_ENUM annotation`() {
        val field = TestEntity::class.java.getDeclaredField("ordinalEnumField")
        
        assertTrue(enumTypeMapper.canHandle(field))
    }

    @Test
    fun `should not handle enum field with NAMED_ENUM annotation`() {
        val field = TestEntity::class.java.getDeclaredField("namedEnumField")
        
        assertFalse(enumTypeMapper.canHandle(field))
    }

    @Test
    fun `should not handle non-enum field`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        
        assertFalse(enumTypeMapper.canHandle(field))
    }

    @Test
    fun `should not handle enum field with other annotation`() {
        val field = TestEntity::class.java.getDeclaredField("jsonEnumField")
        
        assertFalse(enumTypeMapper.canHandle(field))
    }

    @Test
    fun `should handle enum values`() {
        assertTrue(enumTypeMapper.canHandleValue(TestEnum.FIRST))
        assertTrue(enumTypeMapper.canHandleValue(TestEnum.SECOND))
        assertTrue(enumTypeMapper.canHandleValue(TestEnum.THIRD))
    }

    @Test
    fun `should not handle null value`() {
        assertFalse(enumTypeMapper.canHandleValue(null))
    }

    @Test
    fun `should not handle non-enum values`() {
        assertFalse(enumTypeMapper.canHandleValue("string"))
        assertFalse(enumTypeMapper.canHandleValue(42))
        assertFalse(enumTypeMapper.canHandleValue(true))
        assertFalse(enumTypeMapper.canHandleValue(listOf("item")))
    }

    @Test
    fun `should get SQL type for enum field using TypeMapper static method`() {
        val enumField = TestEntity::class.java.getDeclaredField("plainEnumField")
        
        assertEquals(Types.VARCHAR, enumTypeMapper.getSqlTypeForField(enumField))
    }

    // Test enum
    enum class TestEnum {
        FIRST, SECOND, THIRD
    }

    // Test entity class
    class TestEntity {
        var stringField: String = ""
        var plainEnumField: TestEnum = TestEnum.FIRST
        
        @JdbcTypeCode(SqlTypes.ORDINAL_ENUM)
        var ordinalEnumField: TestEnum = TestEnum.FIRST
        
        @JdbcTypeCode(SqlTypes.NAMED_ENUM)
        var namedEnumField: TestEnum = TestEnum.FIRST
        
        @JdbcTypeCode(SqlTypes.JSON)
        var jsonEnumField: TestEnum = TestEnum.FIRST
    }
}