package io.github.mpecan.upsert.model

import io.github.mpecan.upsert.entity.ExtendedTestEntity
import io.github.mpecan.upsert.entity.JpaTestEntity
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class JpaUpsertModelMetadataProviderTest {

    @Test
    fun `should return field  from entity class`() {
        // Given
        val clazz = JpaTestEntity::class.java

        // When
        val result = JpaUpsertModelMetadataProvider.findFieldInClassHierarchy(clazz, "id")

        // Then
        assertNotNull(result)
        assertEquals("id", result!!.name)
        assertEquals(Long::class.java, result.type)
    }

    @Test
    fun `should return null when field is not found in entity class`() {
        // Given
        val clazz = JpaTestEntity::class.java

        // When
        val result = JpaUpsertModelMetadataProvider.findFieldInClassHierarchy(clazz, "invalid")

        // Then
        assertNull(result)
    }

    @Test
    fun `should return the correct field when the field is in the class hierarchy`() {
        // Given
        val clazz = ExtendedTestEntity::class.java

        // When
        val result = JpaUpsertModelMetadataProvider.findFieldInClassHierarchy(clazz, "version")

        // Then
        assertNotNull(result)
        assertEquals("version", result!!.name)
        assertEquals(Int::class.java, result.type)
    }

    @Test
    fun `should return null when the field is not in the class hierarchy`() {
        // Given
        val clazz = ExtendedTestEntity::class.java

        // When
        val result = JpaUpsertModelMetadataProvider.findFieldInClassHierarchy(clazz, "invalid")

        // Then
        assertNull(result)
    }


}