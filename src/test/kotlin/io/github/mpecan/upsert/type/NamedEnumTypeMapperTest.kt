package io.github.mpecan.upsert.type

import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.lang.reflect.Field
import java.sql.Types

class NamedEnumTypeMapperTest {

    private val namedEnumTypeMapper = NamedEnumTypeMapper()

    @Test
    fun `should return null for null value`() {
        val result = namedEnumTypeMapper.convertToJdbcValue(null)
        
        assertNull(result)
    }

    @Test
    fun `should convert enum to name string`() {
        val result = namedEnumTypeMapper.convertToJdbcValue(TestEnum.ACTIVE)
        
        assertEquals("ACTIVE", result)
    }

    @Test
    fun `should convert different enum values to their names`() {
        assertEquals("INACTIVE", namedEnumTypeMapper.convertToJdbcValue(TestEnum.INACTIVE))
        assertEquals("PENDING", namedEnumTypeMapper.convertToJdbcValue(TestEnum.PENDING))
        assertEquals("ACTIVE", namedEnumTypeMapper.convertToJdbcValue(TestEnum.ACTIVE))
    }

    @Test
    fun `should return original value for non-enum types`() {
        val stringValue = "test"
        val intValue = 42
        val booleanValue = true
        
        assertEquals(stringValue, namedEnumTypeMapper.convertToJdbcValue(stringValue))
        assertEquals(intValue, namedEnumTypeMapper.convertToJdbcValue(intValue))
        assertEquals(booleanValue, namedEnumTypeMapper.convertToJdbcValue(booleanValue))
    }

    @Test
    fun `should handle enum field with NAMED_ENUM annotation`() {
        val field = TestEntity::class.java.getDeclaredField("namedEnumField")
        
        assertTrue(namedEnumTypeMapper.canHandle(field))
    }

    @Test
    fun `should not handle enum field without annotation`() {
        val field = TestEntity::class.java.getDeclaredField("plainEnumField")
        
        assertFalse(namedEnumTypeMapper.canHandle(field))
    }

    @Test
    fun `should not handle enum field with ORDINAL_ENUM annotation`() {
        val field = TestEntity::class.java.getDeclaredField("ordinalEnumField")
        
        assertFalse(namedEnumTypeMapper.canHandle(field))
    }

    @Test
    fun `should not handle non-enum field`() {
        val field = TestEntity::class.java.getDeclaredField("stringField")
        
        assertFalse(namedEnumTypeMapper.canHandle(field))
    }

    @Test
    fun `should not handle enum field with other annotation`() {
        val field = TestEntity::class.java.getDeclaredField("jsonEnumField")
        
        assertFalse(namedEnumTypeMapper.canHandle(field))
    }

    @Test
    fun `should handle enum values`() {
        assertTrue(namedEnumTypeMapper.canHandleValue(TestEnum.ACTIVE))
        assertTrue(namedEnumTypeMapper.canHandleValue(TestEnum.INACTIVE))
        assertTrue(namedEnumTypeMapper.canHandleValue(TestEnum.PENDING))
    }

    @Test
    fun `should not handle null value`() {
        assertFalse(namedEnumTypeMapper.canHandleValue(null))
    }

    @Test
    fun `should not handle non-enum values`() {
        assertFalse(namedEnumTypeMapper.canHandleValue("string"))
        assertFalse(namedEnumTypeMapper.canHandleValue(42))
        assertFalse(namedEnumTypeMapper.canHandleValue(true))
        assertFalse(namedEnumTypeMapper.canHandleValue(listOf("item")))
    }

    @Test
    fun `should get SQL type for enum field using TypeMapper static method`() {
        val enumField = TestEntity::class.java.getDeclaredField("namedEnumField")
        
        assertEquals(Types.VARCHAR, namedEnumTypeMapper.getSqlTypeForField(enumField))
    }

    @Test
    fun `should handle complex enum with many values`() {
        val result1 = namedEnumTypeMapper.convertToJdbcValue(ComplexEnum.OPTION_ONE)
        val result2 = namedEnumTypeMapper.convertToJdbcValue(ComplexEnum.OPTION_TWO_WITH_UNDERSCORE)
        val result3 = namedEnumTypeMapper.convertToJdbcValue(ComplexEnum.OPTION_THREE)
        
        assertEquals("OPTION_ONE", result1)
        assertEquals("OPTION_TWO_WITH_UNDERSCORE", result2)
        assertEquals("OPTION_THREE", result3)
    }

    // Test enums
    enum class TestEnum {
        ACTIVE, INACTIVE, PENDING
    }

    enum class ComplexEnum {
        OPTION_ONE, OPTION_TWO_WITH_UNDERSCORE, OPTION_THREE
    }

    // Test entity class
    class TestEntity {
        var stringField: String = ""
        var plainEnumField: TestEnum = TestEnum.ACTIVE
        
        @JdbcTypeCode(SqlTypes.ORDINAL_ENUM)
        var ordinalEnumField: TestEnum = TestEnum.ACTIVE
        
        @JdbcTypeCode(SqlTypes.NAMED_ENUM)
        var namedEnumField: TestEnum = TestEnum.ACTIVE
        
        @JdbcTypeCode(SqlTypes.JSON)
        var jsonEnumField: TestEnum = TestEnum.ACTIVE
    }
}