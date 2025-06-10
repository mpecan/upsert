package io.github.mpecan.upsert.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.RepositoryMetadata
import java.lang.reflect.Method

/**
 * Unit tests for UpsertRepositoryQuery.
 * These tests verify the correct parsing and basic functionality of upsert method calls.
 */
class UpsertRepositoryQueryTest {

    private lateinit var method: Method
    private lateinit var metadata: RepositoryMetadata
    private lateinit var repository: UpsertRepository<Any, Any>
    private lateinit var factory: ProjectionFactory
    private lateinit var repositoryQuery: UpsertRepositoryQuery

    data class TestEntity(val id: Long, val name: String, val active: Boolean)

    @BeforeEach
    fun setUp() {
        metadata = mock()
        repository = mock()
        factory = mock()
        
        // Set up basic mocking for metadata
        whenever(metadata.domainType).thenReturn(TestEntity::class.java)
        whenever(metadata.repositoryInterface).thenReturn(TestRepository::class.java)
    }

    private fun createRepositoryQuery(methodName: String): UpsertRepositoryQuery {
        method = TestRepository::class.java.getMethod(methodName, Any::class.java)
        return UpsertRepositoryQuery(method, metadata, repository, factory)
    }

    private fun createRepositoryQueryForCollection(methodName: String): UpsertRepositoryQuery {
        method = TestRepository::class.java.getMethod(methodName, Collection::class.java)
        return UpsertRepositoryQuery(method, metadata, repository, factory)
    }

    /**
     * Test that UpsertRepositoryQuery can be created successfully.
     */
    @Test
    fun `should create repository query successfully`() {
        // Given/When
        repositoryQuery = createRepositoryQuery("upsert")

        // Then
        assertNotNull(repositoryQuery)
    }

    /**
     * Test that UpsertRepositoryQuery can be created for upsertAll methods.
     */
    @Test
    fun `should create repository query for upsertAll methods`() {
        // Given/When
        repositoryQuery = createRepositoryQueryForCollection("upsertAll")

        // Then
        assertNotNull(repositoryQuery)
    }

    /**
     * Test that UpsertRepositoryQuery can be created for methods with ON clause.
     */
    @Test
    fun `should create repository query for methods with on clause`() {
        // Given/When
        repositoryQuery = createRepositoryQuery("upsertOnName")

        // Then
        assertNotNull(repositoryQuery)
    }

    /**
     * Test that UpsertRepositoryQuery can be created for methods with ignore clause.
     */
    @Test
    fun `should create repository query for methods with ignore clause`() {
        // Given/When
        repositoryQuery = createRepositoryQuery("upsertIgnoringActive")

        // Then
        assertNotNull(repositoryQuery)
    }

    /**
     * Test that UpsertRepositoryQuery can be created for methods with conditional clauses.
     */
    @Test
    fun `should create repository query for methods with conditional clause`() {
        // Given/When
        repositoryQuery = createRepositoryQuery("upsertWhenVersionMoreOrEqual")

        // Then
        assertNotNull(repositoryQuery)
    }

    /**
     * Test exception when no parameters provided.
     */
    @Test
    fun `should throw exception when no parameters provided`() {
        // Given
        repositoryQuery = createRepositoryQuery("upsert")

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            repositoryQuery.execute(emptyArray())
        }
        assertEquals("upsert* methods must have at least one parameter", exception.message)
    }

    /**
     * Test exception when upsertAll called with non-collection parameter.
     */
    @Test
    fun `should throw exception when upsertAll called with non-collection parameter`() {
        // Given
        repositoryQuery = createRepositoryQueryForCollection("upsertAll")
        val nonCollectionParam = "not a collection"

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            repositoryQuery.execute(arrayOf(nonCollectionParam))
        }
        assertEquals("upsertAll* methods must have a collection as first parameter", exception.message)
    }

    /**
     * Test invalid method name throws exception during construction.
     */
    @Test
    fun `should throw exception for invalid method name`() {
        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            method = TestRepository::class.java.getMethod("invalidMethodName", Any::class.java)
            UpsertRepositoryQuery(method, metadata, repository, factory)
        }
        assertTrue(exception.message!!.contains("not a valid upsert method"))
    }

    /**
     * Test that execute method works when given valid parameters.
     */
    @Test
    fun `should execute successfully with valid parameters`() {
        // Given
        repositoryQuery = createRepositoryQuery("upsert")
        val testEntity = TestEntity(1L, "test", true)
        
        // Mock the repository to prevent actual execution
        whenever(repository.upsert(any(), any(), any(), any(), any())).thenReturn(testEntity)

        // When/Then - Just verify no exception is thrown
        assertDoesNotThrow {
            repositoryQuery.execute(arrayOf(testEntity))
        }
    }

    /**
     * Test that execute method works for upsertAll.
     */
    @Test
    fun `should execute successfully for upsertAll with collection`() {
        // Given
        repositoryQuery = createRepositoryQueryForCollection("upsertAll")
        val testEntities = listOf(
            TestEntity(1L, "test1", true),
            TestEntity(2L, "test2", false)
        )
        
        // Mock the repository to prevent actual execution
        whenever(repository.upsertAll(any(), any(), any(), any(), any())).thenReturn(testEntities)

        // When/Then - Just verify no exception is thrown
        assertDoesNotThrow {
            repositoryQuery.execute(arrayOf(testEntities))
        }
    }

    // Test interface to provide method signatures for testing
    interface TestRepository {
        fun upsert(entity: Any): Any
        fun upsertAll(entities: Collection<Any>): Collection<Any>
        fun upsertOnName(entity: Any): Any
        fun upsertIgnoringActive(entity: Any): Any
        fun upsertIgnoringAll(entity: Any): Any
        fun upsertWhenVersionMoreOrEqual(entity: Any): Any
        fun upsertOnNameAndActiveIgnoringDescriptionWhenVersionMore(entity: Any): Any
        fun invalidMethodName(entity: Any): Any
    }
}