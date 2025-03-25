package io.github.mpecan.upsert.type

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class TypeMapperTest {

    @Test
    fun `should map basic Java types correctly`() {
        // Test basic Java types
        assertEquals(Types.VARCHAR, TypeMapper.getSqlType(String::class.java))
        assertEquals(Types.INTEGER, TypeMapper.getSqlType(Int::class.java))
        assertEquals(Types.INTEGER, TypeMapper.getSqlType(Integer::class.java))
        assertEquals(Types.BIGINT, TypeMapper.getSqlType(Long::class.java))
        assertEquals(Types.DOUBLE, TypeMapper.getSqlType(Double::class.java))
        assertEquals(Types.BOOLEAN, TypeMapper.getSqlType(Boolean::class.java))
        assertEquals(Types.DATE, TypeMapper.getSqlType(java.sql.Date::class.java))
        assertEquals(Types.TIMESTAMP, TypeMapper.getSqlType(java.sql.Timestamp::class.java))
    }

    @Test
    fun `should map Java 8 date and time types correctly`() {
        // Test Java 8 date and time types
        assertEquals(Types.DATE, TypeMapper.getSqlType(LocalDate::class.java))
        assertEquals(Types.TIMESTAMP, TypeMapper.getSqlType(LocalDateTime::class.java))
        assertEquals(Types.TIME, TypeMapper.getSqlType(LocalTime::class.java))
    }

    @Test
    fun `should map UUID as VARCHAR`() {
        assertEquals(Types.VARCHAR, TypeMapper.getSqlType(UUID::class.java))
    }
}