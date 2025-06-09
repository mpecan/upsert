package io.github.mpecan.upsert.repository

import io.github.mpecan.upsert.model.ComparisonOperator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for the UpsertMethodNameParser class.
 */
class UpsertMethodNameParserTest {

    private val parser = UpsertMethodNameParser()

    @Test
    fun `should parse upsert method name`() {
        // Given
        val methodName = "upsert"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(emptyList<String>(), info.onFields)
        assertEquals(emptyList<String>(), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
    }

    @Test
    fun `should parse upsertAll method name`() {
        // Given
        val methodName = "upsertAll"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(true, info!!.isUpsertAll)
        assertEquals(emptyList<String>(), info.onFields)
        assertEquals(emptyList<String>(), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
    }

    @Test
    fun `should parse upsertOnName method name`() {
        // Given
        val methodName = "upsertOnName"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(listOf("name"), info.onFields)
        assertEquals(emptyList<String>(), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
    }

    @Test
    fun `should parse upsertOnNameIgnoringActive method name`() {
        // Given
        val methodName = "upsertOnNameIgnoringActive"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(listOf("name"), info.onFields)
        assertEquals(listOf("active"), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
    }

    @Test
    fun `should parse upsertOnNameAndDescriptionIgnoringActive method name`() {
        // Given
        val methodName = "upsertOnNameAndDescriptionIgnoringActive"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(listOf("name", "description"), info.onFields)
        assertEquals(listOf("active"), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
    }

    @Test
    fun `should parse upsertOnNameIgnoringAllFields method name`() {
        // Given
        val methodName = "upsertOnNameIgnoringAllFields"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(listOf("name"), info.onFields)
        assertEquals(emptyList<String>(), info.ignoredFields)
        assertEquals(true, info.ignoreAllFields)
    }

    @Test
    fun `should parse upsertAllOnName method name`() {
        // Given
        val methodName = "upsertAllOnName"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(true, info!!.isUpsertAll)
        assertEquals(listOf("name"), info.onFields)
        assertEquals(emptyList<String>(), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
    }

    @Test
    fun `should parse upsertAllOnNameIgnoringActive method name`() {
        // Given
        val methodName = "upsertAllOnNameIgnoringActive"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(true, info!!.isUpsertAll)
        assertEquals(listOf("name"), info.onFields)
        assertEquals(listOf("active"), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
    }

    @Test
    fun `should parse upsertAllOnNameAndDescriptionIgnoringActive method name`() {
        // Given
        val methodName = "upsertAllOnNameAndDescriptionIgnoringActive"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(true, info!!.isUpsertAll)
        assertEquals(listOf("name", "description"), info.onFields)
        assertEquals(listOf("active"), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
    }

    @Test
    fun `should parse upsertAllOnNameIgnoringAllFields method name`() {
        // Given
        val methodName = "upsertAllOnNameIgnoringAllFields"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(true, info!!.isUpsertAll)
        assertEquals(listOf("name"), info.onFields)
        assertEquals(emptyList<String>(), info.ignoredFields)
        assertEquals(true, info.ignoreAllFields)
    }

    @Test
    fun `should return null for non-upsert method name`() {
        // Given
        val methodName = "findByName"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNull(info)
    }

    @Test
    fun `should convert camel case to snake case`() {
        // Given
        val methodName = "upsertOnAgendaIdAndTypeIgnoringUpdatedAt"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(listOf("agenda_id", "type"), info.onFields)
        assertEquals(listOf("updated_at"), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
    }

    @Test
    fun `should parse upsertOnIdWhenUpdatedAtMore method name`() {
        // Given
        val methodName = "upsertOnIdWhenUpdatedAtMore"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(listOf("id"), info.onFields)
        assertEquals(emptyList<String>(), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
        assertNotNull(info.conditionalInfo)
        assertEquals("updatedAt", info.conditionalInfo!!.fieldName)
        assertEquals(ComparisonOperator.MORE, info.conditionalInfo.operator)
    }

    @Test
    fun `should parse upsertOnIdWhenVersionMoreOrEqual method name`() {
        // Given
        val methodName = "upsertOnIdWhenVersionMoreOrEqual"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(listOf("id"), info.onFields)
        assertEquals(emptyList<String>(), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
        assertNotNull(info.conditionalInfo)
        assertEquals("version", info.conditionalInfo!!.fieldName)
        assertEquals(ComparisonOperator.MORE_OR_EQUAL, info.conditionalInfo.operator)
    }

    @Test
    fun `should parse upsertOnIdWhenPriceLess method name`() {
        // Given
        val methodName = "upsertOnIdWhenPriceLess"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(listOf("id"), info.onFields)
        assertEquals(emptyList<String>(), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
        assertNotNull(info.conditionalInfo)
        assertEquals("price", info.conditionalInfo!!.fieldName)
        assertEquals(ComparisonOperator.LESS, info.conditionalInfo.operator)
    }

    @Test
    fun `should parse upsertOnIdWhenScoreLessOrEqual method name`() {
        // Given
        val methodName = "upsertOnIdWhenScoreLessOrEqual"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(listOf("id"), info.onFields)
        assertEquals(emptyList<String>(), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
        assertNotNull(info.conditionalInfo)
        assertEquals("score", info.conditionalInfo!!.fieldName)
        assertEquals(ComparisonOperator.LESS_OR_EQUAL, info.conditionalInfo.operator)
    }

    @Test
    fun `should parse upsertAllOnIdWhenUpdatedAtMoreIgnoringCreatedAt method name`() {
        // Given
        val methodName = "upsertAllOnIdWhenUpdatedAtMoreIgnoringCreatedAt"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(true, info!!.isUpsertAll)
        assertEquals(listOf("id"), info.onFields)
        assertEquals(listOf("created_at"), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
        assertNotNull(info.conditionalInfo)
        assertEquals("updatedAt", info.conditionalInfo!!.fieldName)
        assertEquals(ComparisonOperator.MORE, info.conditionalInfo.operator)
    }

    @Test
    fun `should parse upsertOnNameAndDescriptionWhenVersionMoreOrEqualIgnoringActive method name`() {
        // Given
        val methodName = "upsertOnNameAndDescriptionWhenVersionMoreOrEqualIgnoringActive"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertEquals(false, info!!.isUpsertAll)
        assertEquals(listOf("name", "description"), info.onFields)
        assertEquals(listOf("active"), info.ignoredFields)
        assertEquals(false, info.ignoreAllFields)
        assertNotNull(info.conditionalInfo)
        assertEquals("version", info.conditionalInfo!!.fieldName)
        assertEquals(ComparisonOperator.MORE_OR_EQUAL, info.conditionalInfo.operator)
    }

    @Test
    fun `should parse conditional field with camel intact`() {
        // Given
        val methodName = "upsertOnIdWhenUpdatedAtAndVersionMore"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertNotNull(info!!.conditionalInfo)
        assertEquals("updatedAtAndVersion", info.conditionalInfo!!.fieldName)
        assertEquals(ComparisonOperator.MORE, info.conditionalInfo.operator)
    }

    @Test
    fun `should return null conditional info for method without When clause`() {
        // Given
        val methodName = "upsertOnIdIgnoringActive"

        // When
        val info = parser.parse(methodName)

        // Then
        assertNotNull(info)
        assertNull(info!!.conditionalInfo)
    }
}