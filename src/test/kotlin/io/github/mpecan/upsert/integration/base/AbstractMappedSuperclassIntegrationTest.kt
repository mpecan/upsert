package io.github.mpecan.upsert.integration.base

import io.github.mpecan.upsert.entity.ExtendedTestEntity
import io.github.mpecan.upsert.entity.PureInheritanceTestEntity
import io.github.mpecan.upsert.integration.repositories.ExtendedTestEntityRepository
import io.github.mpecan.upsert.integration.repositories.PureInheritanceTestEntityRepository
import io.github.mpecan.upsert.model.JpaUpsertModelMetadataProvider
import io.github.mpecan.upsert.type.TypeMapperRegistry
import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Abstract integration test class for @MappedSuperclass support.
 * This class provides common test scenarios for entities that inherit fields from
 * classes annotated with @MappedSuperclass.
 * 
 * Database-specific implementations should extend this class to run the tests
 * against their specific database engines (MySQL, PostgreSQL, etc.).
 */
abstract class AbstractMappedSuperclassIntegrationTest : AbstractRepositoryIntegrationTest() {

    @Autowired
    private lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    private lateinit var typeMapperRegistry: TypeMapperRegistry

    @Autowired
    private lateinit var extendedTestEntityRepository: ExtendedTestEntityRepository

    @Autowired
    private lateinit var pureInheritanceTestEntityRepository: PureInheritanceTestEntityRepository

    @Test
    fun `should discover fields from MappedSuperclass in JPA metadata provider`() {
        // Given
        val metamodel = entityManagerFactory.metamodel
        val persistenceUnitUtil = entityManagerFactory.persistenceUnitUtil
        
        val metadataProvider = JpaUpsertModelMetadataProvider(
            metamodel,
            persistenceUnitUtil,
            ExtendedTestEntity::class.java,
            typeMapperRegistry
        )

        // When
        val columns = metadataProvider.getColumns()
        val fieldNames = columns.map { it.fieldName }.toSet()

        // Then - should include fields from both the entity and the @MappedSuperclass
        assertThat(fieldNames).contains(
            // Fields from ExtendedTestEntity
            "id", "name", "description",
            // Fields from BaseEntity (@MappedSuperclass)
            "createdAt", "updatedAt", "version"
        )
        
        // Verify we have at least the expected number of columns (6 total)
        assertThat(columns.size).isGreaterThanOrEqualTo(6)
    }

    @Test
    fun `should discover fields from MappedSuperclass for pure inheritance entity`() {
        // Given
        val metamodel = entityManagerFactory.metamodel
        val persistenceUnitUtil = entityManagerFactory.persistenceUnitUtil
        
        val metadataProvider = JpaUpsertModelMetadataProvider(
            metamodel,
            persistenceUnitUtil,
            PureInheritanceTestEntity::class.java,
            typeMapperRegistry
        )

        // When
        val columns = metadataProvider.getColumns()
        val fieldNames = columns.map { it.fieldName }.toSet()

        // Then - should include fields from both entity and @MappedSuperclass
        assertThat(fieldNames).contains(
            // Fields from PureInheritanceTestEntity
            "id", "name", "description",
            // Fields from BaseEntity (@MappedSuperclass) - inherited only
            "createdAt", "updatedAt", "version"
        )
        
        // Verify we have at least the expected number of columns (6 total)
        assertThat(columns.size).isGreaterThanOrEqualTo(6)
    }

    @Test
    fun `should upsert ExtendedTestEntity with inherited fields`() {
        // Given - create entities with inherited fields
        val now = LocalDateTime.now()
        val entity1 = ExtendedTestEntity(
            id = 1L,
            name = "Extended Entity 1",
            description = "First extended test entity",
            createdAt = now,
            updatedAt = now,
            version = 1
        )
        val entity2 = ExtendedTestEntity(
            id = 2L,
            name = "Extended Entity 2", 
            description = "Second extended test entity",
            createdAt = now,
            updatedAt = now,
            version = 1
        )

        // When - perform upsert operations
        extendedTestEntityRepository.upsertAll(listOf(entity1, entity2))

        // Then - entities should be saved successfully
        val savedEntities = extendedTestEntityRepository.findAll()
        assertThat(savedEntities).hasSize(2)
        
        val savedEntity1 = savedEntities.find { it.id == 1L }
        assertThat(savedEntity1).isNotNull
        assertThat(savedEntity1!!.name).isEqualTo("Extended Entity 1")
        assertThat(savedEntity1.createdAt).isEqualTo(now)
        assertThat(savedEntity1.version).isEqualTo(1)

        val savedEntity2 = savedEntities.find { it.id == 2L }
        assertThat(savedEntity2).isNotNull
        assertThat(savedEntity2!!.name).isEqualTo("Extended Entity 2")
        assertThat(savedEntity2.createdAt).isEqualTo(now)
        assertThat(savedEntity2.version).isEqualTo(1)
    }

    @Test
    fun `should upsert PureInheritanceTestEntity with inherited fields`() {
        // Given - create entities that rely purely on inheritance
        val entity1 = PureInheritanceTestEntity(
            id = 10L,
            name = "Pure Inheritance Entity 1",
            description = "First pure inheritance test entity"
        )
        val entity2 = PureInheritanceTestEntity(
            id = 20L,
            name = "Pure Inheritance Entity 2", 
            description = "Second pure inheritance test entity"
        )

        // When - perform upsert operations
        pureInheritanceTestEntityRepository.upsertAll(listOf(entity1, entity2))

        // Then - entities should be saved successfully with inherited fields
        val savedEntities = pureInheritanceTestEntityRepository.findAll()
        assertThat(savedEntities).hasSize(2)
        
        val savedEntity1 = savedEntities.find { it.id == 10L }
        assertThat(savedEntity1).isNotNull
        assertThat(savedEntity1!!.name).isEqualTo("Pure Inheritance Entity 1")
        // Inherited fields should have default values from BaseEntity
        assertThat(savedEntity1.createdAt).isNotNull
        assertThat(savedEntity1.updatedAt).isNotNull
        assertThat(savedEntity1.version).isEqualTo(1)

        val savedEntity2 = savedEntities.find { it.id == 20L }
        assertThat(savedEntity2).isNotNull
        assertThat(savedEntity2!!.name).isEqualTo("Pure Inheritance Entity 2")
        assertThat(savedEntity2.createdAt).isNotNull
        assertThat(savedEntity2.updatedAt).isNotNull
        assertThat(savedEntity2.version).isEqualTo(1)
    }

    @Test
    fun `should update existing entities with inherited fields`() {
        // Given - save initial entities
        val now = LocalDateTime.now()
        val initialEntity = ExtendedTestEntity(
            id = 100L,
            name = "Initial Name",
            description = "Initial description",
            createdAt = now,
            updatedAt = now,
            version = 1
        )
        extendedTestEntityRepository.upsert(initialEntity)

        // When - update with new values including inherited fields
        val laterTime = now.plusHours(1)
        val updatedEntity = ExtendedTestEntity(
            id = 100L,  // Same ID for update
            name = "Updated Name",
            description = "Updated description",
            createdAt = now,  // Keep original created time
            updatedAt = laterTime,  // Update the modified time
            version = 2  // Increment version
        )
        extendedTestEntityRepository.upsert(updatedEntity)

        // Then - entity should be updated with new values
        val savedEntity = extendedTestEntityRepository.findById(100L).orElse(null)
        assertThat(savedEntity).isNotNull
        assertThat(savedEntity!!.name).isEqualTo("Updated Name")
        assertThat(savedEntity.description).isEqualTo("Updated description")
        assertThat(savedEntity.createdAt).isEqualTo(now)
        assertThat(savedEntity.updatedAt).isEqualTo(laterTime)
        assertThat(savedEntity.version).isEqualTo(2)
    }

    @Test
    fun `should handle mixed insert and update operations with inherited fields`() {
        // Given - save one entity initially
        val now = LocalDateTime.now()
        val existingEntity = PureInheritanceTestEntity(
            id = 200L,
            name = "Existing Entity",
            description = "Already exists"
        )
        pureInheritanceTestEntityRepository.upsert(existingEntity)

        // When - perform upsert with mix of new and existing entities
        val newEntity = PureInheritanceTestEntity(
            id = 201L,
            name = "New Entity",
            description = "Brand new"
        )
        val updatedExistingEntity = PureInheritanceTestEntity(
            id = 200L,  // Same ID as existing
            name = "Updated Existing Entity",
            description = "Updated description"
        )

        pureInheritanceTestEntityRepository.upsertAll(listOf(newEntity, updatedExistingEntity))

        // Then - should have both entities with correct values
        val allEntities = pureInheritanceTestEntityRepository.findAll()
        val relevantEntities = allEntities.filter { it.id in listOf(200L, 201L) }
        assertThat(relevantEntities).hasSize(2)

        val existingUpdated = relevantEntities.find { it.id == 200L }
        assertThat(existingUpdated).isNotNull
        assertThat(existingUpdated!!.name).isEqualTo("Updated Existing Entity")
        assertThat(existingUpdated.description).isEqualTo("Updated description")

        val newSaved = relevantEntities.find { it.id == 201L }
        assertThat(newSaved).isNotNull
        assertThat(newSaved!!.name).isEqualTo("New Entity")
        assertThat(newSaved.description).isEqualTo("Brand new")
        
        // All entities should have inherited fields properly set
        relevantEntities.forEach { entity ->
            assertThat(entity.createdAt).isNotNull
            assertThat(entity.updatedAt).isNotNull
            assertThat(entity.version).isEqualTo(1)
        }
    }
}