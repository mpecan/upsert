package io.github.mpecan.upsert.integration.base

import io.github.mpecan.upsert.entity.JpaTestEntity
import io.github.mpecan.upsert.integration.TestApplication
import io.github.mpecan.upsert.integration.repositories.CustomMethodsTestRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Integration tests for custom upsert methods.
 * This class tests the method name parsing functionality.
 */
@SpringBootTest(classes = [TestApplication::class])
@Testcontainers
@Transactional
abstract class AbstractCustomMethodsIntegrationTest {

    @Autowired
    private lateinit var customMethodsTestRepository: CustomMethodsTestRepository

    @Test
    fun `should upsert successfully using id as ON clause`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(1, "Test Entity", "Updated Description", false)

        // Insert the first entity
        customMethodsTestRepository.upsertOnId(entity1)

        // When
        val result = customMethodsTestRepository.upsertOnId(entity2)

        // Then
        Assertions.assertEquals(entity1.name, result.name)
        Assertions.assertEquals(entity2.description, result.description)
        Assertions.assertEquals(entity2.active, result.active)
    }

    @Test
    fun `should upsert successfully ignoring name as IGNORING clause`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Updated Description", false)

        // Insert the first entity
        customMethodsTestRepository.upsert(entity1)

        // When
        val result = customMethodsTestRepository.upsertIgnoringName(entity2)

        // Then
        Assertions.assertEquals(entity2.name, result.name)
        Assertions.assertEquals(entity2.description, result.description)
        Assertions.assertEquals(entity2.active, result.active)
    }

    @Test
    fun `should throw exception when using name as ON clause without uniqueness constraint`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Updated Description", false)

        // Insert the first entity
        customMethodsTestRepository.upsert(entity1)

        // When/Then - expect exception when using name as ON clause
        val exception = Assertions.assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertOnName(entity2)
        }

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
        assert(exception.message?.contains("name") == true)
    }

    @Test
    fun `should throw exception when using name as ON clause and ignoring active field without uniqueness constraint`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Updated Description", false)

        // Insert the first entity
        customMethodsTestRepository.upsert(entity1)

        // When/Then - expect exception when using name as ON clause
        val exception = Assertions.assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertOnNameIgnoringActive(entity2)
        }

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
        assert(exception.message?.contains("name") == true)
    }

    @Test
    fun `should throw exception when using name and description as ON clause without uniqueness constraint`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Description A", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Description B", false)

        // Insert the first entity
        customMethodsTestRepository.upsert(entity1)

        // When/Then - expect exception when using name and description as ON clause
        val exception = Assertions.assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertOnNameAndDescription(entity2)
        }

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
        assert(exception.message?.contains("name, description") == true)
    }

    @Test
    fun `should throw exception when using name as ON clause and ignoring all fields without uniqueness constraint`() {
        // Given
        val entity1 = JpaTestEntity(1, "Test Entity", "Original Description", true)
        val entity2 = JpaTestEntity(2, "Test Entity", "Updated Description", false)

        // Insert the first entity
        customMethodsTestRepository.upsert(entity1)

        // When/Then - expect exception when using name as ON clause and ignoring all fields
        Assertions.assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertOnNameIgnoringAllFields(entity2)
        }

    }

    @Test
    fun `should throw exception when using name as ON clause for multiple entities without uniqueness constraint`() {
        // Given
        val entities1 = listOf(
            JpaTestEntity(1, "Entity A", "Description 1", true),
            JpaTestEntity(2, "Entity B", "Description 2", true)
        )

        val entities2 = listOf(
            JpaTestEntity(3, "Entity A", "Updated Description 1", false),
            JpaTestEntity(4, "Entity B", "Updated Description 2", false),
            JpaTestEntity(5, "Entity C", "Description 3", true)
        )

        // Insert the first set of entities
        customMethodsTestRepository.upsertAll(entities1)

        // When/Then - expect exception when using name as ON clause
        val exception = Assertions.assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertAllOnName(entities2)
        }

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
        assert(exception.message?.contains("name") == true)
    }

    @Test
    fun `should throw exception when using name as ON clause for multiple entities and ignoring active field without uniqueness constraint`() {
        // Given
        val entities1 = listOf(
            JpaTestEntity(1, "Entity A", "Description 1", true),
            JpaTestEntity(2, "Entity B", "Description 2", true)
        )

        val entities2 = listOf(
            JpaTestEntity(3, "Entity A", "Updated Description 1", false),
            JpaTestEntity(4, "Entity B", "Updated Description 2", false)
        )

        // Insert the first set of entities
        customMethodsTestRepository.upsertAll(entities1)

        // When/Then - expect exception when using name as ON clause
        val exception = Assertions.assertThrows(InvalidDataAccessApiUsageException::class.java) {
            customMethodsTestRepository.upsertAllOnNameIgnoringActive(entities2)
        }

        // Verify the exception message
        assert(exception.message?.contains("do not have a uniqueness or exclusion constraint") == true)
        assert(exception.message?.contains("name") == true)
    }
}