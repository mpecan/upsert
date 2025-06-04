package io.github.mpecan.upsert.integration.base

import io.github.mpecan.upsert.entity.ConditionalTestEntity
import io.github.mpecan.upsert.integration.TestApplication
import io.github.mpecan.upsert.integration.repositories.ConditionalTestRepository
import org.junit.jupiter.api.Assertions.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

/**
 * Abstract integration tests for conditional upsert operations.
 * Tests the conditional WHERE clause functionality with different comparison operators.
 */
@SpringBootTest(classes = [TestApplication::class])
@Testcontainers
@Transactional
abstract class AbstractConditionalUpsertIntegrationTest {

    private val logger = LoggerFactory.getLogger(AbstractConditionalUpsertIntegrationTest::class.java)

    @Autowired
    protected lateinit var conditionalTestRepository: ConditionalTestRepository

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        try {
            // Clear the table before each test
            jdbcTemplate.execute("DELETE FROM conditional_test_entity")
            logger.info("Table cleared successfully")
        } catch (e: Exception) {
            logger.error("Error clearing table", e)
            throw e
        }
    }

    @Test
    fun `test simple version update without condition`() {
        // Given - insert initial entity
        val entity1 = ConditionalTestEntity(
            id = 100L,
            name = "Test",
            version = 1,
            price = 100.0,
            score = 5.0f,
            updatedAt = LocalDateTime.now(),
            description = "Test"
        )
        conditionalTestRepository.upsert(entity1)
        
        // When - update version
        val entity2 = entity1.copy(version = 99)
        conditionalTestRepository.upsert(entity2)
        
        // Then - verify version was updated
        val stored = getEntityById(100L)
        assertEquals(99, stored!!.version)
    }


    @Test
    fun `should update only when updatedAt is more recent using More operator`() {
        // Given - insert initial entity
        val initialTime = LocalDateTime.now().minusHours(1)
        val entity1 = ConditionalTestEntity(
            id = 1L,
            name = "Original",
            version = 1,
            price = 100.0,
            score = 8.5f,
            updatedAt = initialTime,
            description = "Original description"
        )
        conditionalTestRepository.upsert(entity1)
        
        // Verify initial state
        val initialStored = getEntityById(1L)
        logger.info("Initial state after insert: version=${initialStored!!.version}, updatedAt=${initialStored.updatedAt}")

        // When - try to update with older timestamp (should NOT update)
        val olderTime = initialTime.minusHours(1)
        val entity2 = entity1.copy(
            name = "Should Not Update",
            version = 2,
            updatedAt = olderTime
        )
        logger.info("First update attempt with older timestamp: entity version=${entity2.version}, updatedAt=$olderTime")
        val result1 = conditionalTestRepository.upsertOnIdWhenUpdatedAtMore(entity2)

        // Then - verify no update occurred
        val stored1 = jdbcTemplate.queryForObject(
            "SELECT * FROM conditional_test_entity WHERE id = ?",
            { rs, _ ->
                ConditionalTestEntity(
                    id = rs.getLong("id"),
                    name = rs.getString("name"),
                    version = rs.getInt("version"),
                    price = rs.getDouble("price"),
                    score = rs.getFloat("score"),
                    updatedAt = rs.getTimestamp("updated_at").toLocalDateTime(),
                    description = rs.getString("description"),
                    active = rs.getBoolean("active")
                )
            },
            1L
        )
        assertEquals("Original", stored1!!.name)
        assertEquals(1, stored1.version)
        assertThat(stored1.updatedAt).isEqualToIgnoringNanos(initialTime)

        // When - try to update with newer timestamp (should update)
        val newerTime = initialTime.plusHours(1)
        logger.info("Creating newer time: initialTime=$initialTime, newerTime=$newerTime")
        val entity3 = entity1.copy(
            name = "Updated Successfully",
            version = 3,
            updatedAt = newerTime
        )
        logger.info("Second update attempt with newer timestamp: entity version=${entity3.version}, updatedAt=${entity3.updatedAt}")
        val result2 = conditionalTestRepository.upsertOnIdWhenUpdatedAtMore(entity3)
        
        // Debug: Check what was returned from the upsert
        logger.info("Returned entity from upsert: id=${result2.id}, name=${result2.name}, version=${result2.version}")

        // Then - verify update occurred
        val stored2 = jdbcTemplate.queryForObject(
            "SELECT * FROM conditional_test_entity WHERE id = ?",
            { rs, _ ->
                ConditionalTestEntity(
                    id = rs.getLong("id"),
                    name = rs.getString("name"),
                    version = rs.getInt("version"),
                    price = rs.getDouble("price"),
                    score = rs.getFloat("score"),
                    updatedAt = rs.getTimestamp("updated_at").toLocalDateTime(),
                    description = rs.getString("description"),
                    active = rs.getBoolean("active")
                )
            },
            1L
        )
        
        // Debug output
        logger.info("After conditional update with newer timestamp:")
        logger.info("  Name: ${stored2!!.name} (expected: 'Updated Successfully')")
        logger.info("  Version: ${stored2.version} (expected: 3)")
        logger.info("  UpdatedAt: ${stored2.updatedAt} (expected: $newerTime)")
        
        // Debug: Execute raw SQL to check what's happening
        val testUpdate = jdbcTemplate.update(
            "UPDATE conditional_test_entity SET version = 999 WHERE id = 1"
        )
        logger.info("Direct update result: $testUpdate")
        val afterDirectUpdate = jdbcTemplate.queryForObject(
            "SELECT version FROM conditional_test_entity WHERE id = ?",
            Integer::class.java,
            1L
        )
        logger.info("Version after direct update: $afterDirectUpdate")
        
        assertEquals("Updated Successfully", stored2.name)
        assertEquals(3, stored2.version)
        assertThat(stored2.updatedAt).isEqualToIgnoringNanos(newerTime)
    }

    @Test
    fun `should update only when version is greater or equal using MoreOrEqual operator`() {
        // Given - insert initial entity
        val entity1 = ConditionalTestEntity(
            id = 2L,
            name = "Version Test",
            version = 5,
            price = 200.0,
            score = 7.0f,
            updatedAt = LocalDateTime.now(),
            description = "Version test"
        )
        conditionalTestRepository.upsert(entity1)

        // When - try to update with lower version (should NOT update)
        val entity2 = entity1.copy(name = "Lower Version", version = 3)
        conditionalTestRepository.upsertOnIdWhenVersionMoreOrEqual(entity2)

        // Then - verify no update
        val stored1 = getEntityById(2L)
        assertEquals("Version Test", stored1!!.name)
        assertEquals(5, stored1.version)

        // When - try to update with equal version (should update)
        val entity3 = entity1.copy(name = "Equal Version", version = 5)
        conditionalTestRepository.upsertOnIdWhenVersionMoreOrEqual(entity3)

        // Then - verify update occurred
        val stored2 = getEntityById(2L)
        assertEquals("Equal Version", stored2!!.name)
        assertEquals(5, stored2.version)

        // When - try to update with higher version (should update)
        val entity4 = entity1.copy(name = "Higher Version", version = 7)
        conditionalTestRepository.upsertOnIdWhenVersionMoreOrEqual(entity4)

        // Then - verify update occurred
        val stored3 = getEntityById(2L)
        assertEquals("Higher Version", stored3!!.name)
        assertEquals(7, stored3.version)
    }

    @Test
    fun `should update only when price is less using Less operator`() {
        // Given - insert initial entity
        val entity1 = ConditionalTestEntity(
            id = 3L,
            name = "Price Test",
            version = 1,
            price = 100.0,
            score = 5.0f,
            updatedAt = LocalDateTime.now()
        )
        conditionalTestRepository.upsert(entity1)

        // When - try to update with higher price (should NOT update)
        val entity2 = entity1.copy(name = "Higher Price", price = 150.0)
        conditionalTestRepository.upsertOnIdWhenPriceLess(entity2)

        // Then - verify no update
        val stored1 = getEntityById(3L)
        assertEquals("Price Test", stored1!!.name)
        assertEquals(100.0, stored1.price)

        // When - try to update with lower price (should update)
        val entity3 = entity1.copy(name = "Lower Price", price = 80.0)
        conditionalTestRepository.upsertOnIdWhenPriceLess(entity3)

        // Then - verify update occurred
        val stored2 = getEntityById(3L)
        assertEquals("Lower Price", stored2!!.name)
        assertEquals(80.0, stored2.price)
    }

    @Test
    fun `should update only when score is less or equal using LessOrEqual operator`() {
        // Given - insert initial entity
        val entity1 = ConditionalTestEntity(
            id = 4L,
            name = "Score Test",
            version = 1,
            price = 100.0,
            score = 7.5f,
            updatedAt = LocalDateTime.now()
        )
        conditionalTestRepository.upsert(entity1)

        // When - try to update with higher score (should NOT update)
        val entity2 = entity1.copy(name = "Higher Score", score = 8.5f)
        conditionalTestRepository.upsertOnIdWhenScoreLessOrEqual(entity2)

        // Then - verify no update
        val stored1 = getEntityById(4L)
        assertEquals("Score Test", stored1!!.name)
        assertEquals(7.5f, stored1.score)

        // When - try to update with equal score (should update)
        val entity3 = entity1.copy(name = "Equal Score", score = 7.5f)
        conditionalTestRepository.upsertOnIdWhenScoreLessOrEqual(entity3)

        // Then - verify update occurred
        val stored2 = getEntityById(4L)
        assertEquals("Equal Score", stored2!!.name)
        assertEquals(7.5f, stored2.score)

        // When - try to update with lower score (should update)
        val entity4 = entity1.copy(name = "Lower Score", score = 6.0f)
        conditionalTestRepository.upsertOnIdWhenScoreLessOrEqual(entity4)

        // Then - verify update occurred
        val stored3 = getEntityById(4L)
        assertEquals("Lower Score", stored3!!.name)
        assertEquals(6.0f, stored3.score)
    }

    @Test
    fun `should handle conditional upsert with ignoring fields`() {
        // Given - insert initial entity
        val entity1 = ConditionalTestEntity(
            id = 5L,
            name = "Ignore Test",
            version = 1,
            price = 100.0,
            score = 5.0f,
            updatedAt = LocalDateTime.now(),
            description = "Original description"
        )
        conditionalTestRepository.upsert(entity1)

        // When - update with higher version and different description (description should be ignored)
        val entity2 = entity1.copy(
            name = "Updated Name",
            version = 2,
            description = "This should be ignored"
        )
        conditionalTestRepository.upsertOnIdWhenVersionMoreIgnoringDescription(entity2)

        // Then - verify update occurred but description was not changed
        val stored = getEntityById(5L)
        assertEquals("Updated Name", stored!!.name)
        assertEquals(2, stored.version)
        assertEquals("Original description", stored.description) // Description should remain unchanged
    }

    @Test
    fun `should handle batch conditional upserts`() {
        // Given - insert initial entities
        val baseTime = LocalDateTime.now()
        val entities = listOf(
            ConditionalTestEntity(
                id = 10L,
                name = "Entity 1",
                version = 1,
                price = 100.0,
                score = 5.0f,
                updatedAt = baseTime.minusHours(2)
            ),
            ConditionalTestEntity(
                id = 11L,
                name = "Entity 2",
                version = 1,
                price = 200.0,
                score = 6.0f,
                updatedAt = baseTime.minusHours(1)
            ),
            ConditionalTestEntity(
                id = 12L,
                name = "Entity 3",
                version = 1,
                price = 300.0,
                score = 7.0f,
                updatedAt = baseTime
            )
        )
        conditionalTestRepository.upsertAll(entities)

        // When - try to update with mixed timestamps
        val updates = listOf(
            // Older timestamp - should NOT update
            entities[0].copy(name = "Should NOT Update 1", updatedAt = baseTime.minusHours(3)),
            // Newer timestamp - should update
            entities[1].copy(name = "Should Update 2", updatedAt = baseTime.plusHours(1)),
            // Older timestamp - should NOT update
            entities[2].copy(name = "Should NOT Update 3", updatedAt = baseTime.minusHours(1))
        )
        val results = conditionalTestRepository.upsertAllOnIdWhenUpdatedAtMore(updates)

        // Then - verify selective updates
        val stored1 = getEntityById(10L)
        assertEquals("Entity 1", stored1!!.name) // Not updated
        
        val stored2 = getEntityById(11L)
        assertEquals("Should Update 2", stored2!!.name) // Updated
        
        val stored3 = getEntityById(12L)
        assertEquals("Entity 3", stored3!!.name) // Not updated
    }

    @Test
    fun `should handle complex conditional upserts with multiple conditions`() {
        // Given - create entities with same name and version for testing composite ON clause
        val baseTime = LocalDateTime.now()
        
        // First, insert an entity
        val entity1 = ConditionalTestEntity(
            id = 20L,
            name = "Complex Test",
            version = 5,
            price = 100.0,
            score = 8.0f,
            updatedAt = baseTime,
            active = true
        )
        conditionalTestRepository.upsert(entity1)

        // When - try to update with same name and version but older timestamp (should NOT update)
        val entity2 = ConditionalTestEntity(
            id = 21L, // Different ID
            name = "Complex Test",
            version = 5,
            price = 200.0,
            score = 9.0f,
            updatedAt = baseTime.minusHours(1),
            active = false // This should be ignored
        )
        conditionalTestRepository.upsertOnNameAndVersionWhenUpdatedAtMoreOrEqualIgnoringActive(entity2)

        // Then - verify original entity unchanged
        val stored1 = getEntityById(20L)
        assertEquals(100.0, stored1!!.price)
        assertEquals(8.0f, stored1.score)
        assertEquals(true, stored1.active) // Active field should remain unchanged

        // When - try to update with newer timestamp (should update)
        val entity3 = entity1.copy(
            price = 300.0,
            score = 10.0f,
            updatedAt = baseTime.plusHours(1),
            active = false // This should still be ignored
        )
        conditionalTestRepository.upsertOnNameAndVersionWhenUpdatedAtMoreOrEqualIgnoringActive(entity3)

        // Then - verify update occurred but active field was ignored
        val stored2 = getEntityById(20L)
        assertEquals(300.0, stored2!!.price)
        assertEquals(10.0f, stored2.score)
        assertEquals(true, stored2.active) // Active should still be true (ignored)
        assertThat(stored2.updatedAt).isEqualToIgnoringNanos(baseTime.plusHours(1))
    }

    @Test
    fun `should insert new entities regardless of conditional clause`() {
        // When - insert a new entity with conditional method
        val newEntity = ConditionalTestEntity(
            id = 30L,
            name = "New Entity",
            version = 1,
            price = 500.0,
            score = 9.5f,
            updatedAt = LocalDateTime.now()
        )
        val result = conditionalTestRepository.upsertOnIdWhenUpdatedAtMore(newEntity)

        // Then - verify entity was inserted
        val stored = getEntityById(30L)
        assertNotNull(stored)
        assertEquals("New Entity", stored!!.name)
        assertEquals(1, stored.version)
        assertEquals(500.0, stored.price)
    }

    private fun getEntityById(id: Long): ConditionalTestEntity? {
        return try {
            jdbcTemplate.queryForObject(
                "SELECT * FROM conditional_test_entity WHERE id = ?",
                { rs, _ ->
                    ConditionalTestEntity(
                        id = rs.getLong("id"),
                        name = rs.getString("name"),
                        version = rs.getInt("version"),
                        price = rs.getDouble("price"),
                        score = rs.getFloat("score"),
                        updatedAt = rs.getTimestamp("updated_at").toLocalDateTime(),
                        description = rs.getString("description"),
                        active = rs.getBoolean("active")
                    )
                },
                id
            )
        } catch (e: Exception) {
            null
        }
    }
}