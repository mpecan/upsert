package io.github.mpecan.upsert.type.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.gson.Gson
import io.github.mpecan.upsert.dialect.testTypeProvider
import io.github.mpecan.upsert.type.TypeMapperRegistry
import io.github.mpecan.upsert.type.json.test.TestJsonEntity
import io.github.mpecan.upsert.type.json.test.TestMetadata
import io.github.mpecan.upsert.type.json.test.TestNestedData
import jakarta.json.bind.JsonbBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.sql.Types
import java.time.LocalDateTime

/**
 * Unit tests for the JSON type mappers.
 * Tests all three implementations (Jackson, Gson, JSON-B) and the fallback implementation.
 */
class JsonTypeMapperTest {

    // Test data
    private lateinit var testEntity: TestJsonEntity
    private lateinit var attributesField: Field
    private lateinit var tagsField: Field
    private lateinit var metadataField: Field
    private lateinit var createdAtField: Field

    @BeforeEach
    fun setUp() {
        // Create test entity
        testEntity = TestJsonEntity(
            id = 1L,
            name = "Test Entity",
            attributes = mapOf("key1" to "value1", "key2" to "value2"),
            tags = listOf("tag1", "tag2", "tag3"),
            metadata = TestMetadata(
                version = "1.0.0",
                description = "Test metadata",
                active = true,
                settings = mapOf("setting1" to "value1"),
                scores = listOf(10, 20, 30),
                nested = TestNestedData("nested1", 42.5)
            ),
            createdAt = LocalDateTime.now()
        )

        // Get fields for testing
        val entityClass = TestJsonEntity::class.java
        attributesField = entityClass.getDeclaredField("attributes")
        tagsField = entityClass.getDeclaredField("tags")
        metadataField = entityClass.getDeclaredField("metadata")
        createdAtField = entityClass.getDeclaredField("createdAt")
    }

    /**
     * Test the Jackson JSON mapper implementation.
     */
    @Test
    fun testJacksonJsonMapper() {
        // Create Jackson ObjectMapper and JsonTypeMapper
        val objectMapper = ObjectMapper().registerKotlinModule()
        val jacksonMapper = JacksonJsonTypeMapper(objectMapper, Types.VARCHAR)

        // Test field detection
        assertTrue(jacksonMapper.isJsonField(attributesField))
        assertTrue(jacksonMapper.isJsonField(tagsField))
        assertTrue(jacksonMapper.isJsonField(metadataField))
        assertFalse(jacksonMapper.isJsonField(createdAtField))

        // Test SQL type determination
        assertEquals(Types.VARCHAR, jacksonMapper.getSqlTypeForField(attributesField))
        assertEquals(Types.VARCHAR, jacksonMapper.getSqlTypeForField(tagsField))
        assertEquals(Types.VARCHAR, jacksonMapper.getSqlTypeForField(metadataField))

        // Test JSON serialization
        val jsonAttributes = jacksonMapper.toJson(testEntity.attributes)
        assertTrue(jsonAttributes.contains("key1"))
        assertTrue(jsonAttributes.contains("value1"))

        val jsonMetadata = jacksonMapper.toJson(testEntity.metadata)
        assertTrue(jsonMetadata.contains("version"))
        assertTrue(jsonMetadata.contains("1.0.0"))
        assertTrue(jsonMetadata.contains("nested"))
        assertTrue(jsonMetadata.contains("42.5"))

        // Test value handling
        assertFalse(jacksonMapper.canHandleValue(null))
        assertTrue(jacksonMapper.canHandleValue(testEntity.attributes))
        assertTrue(jacksonMapper.canHandleValue(testEntity.metadata))
        assertFalse(jacksonMapper.canHandleValue(testEntity.createdAt))

        // Test JDBC value conversion
        val jdbcValue = jacksonMapper.convertToJdbcValue(testEntity.metadata)
        assertTrue(jdbcValue is String)
    }

    /**
     * Test the Gson JSON mapper implementation.
     */
    @Test
    fun testGsonJsonMapper() {
        // Create Gson and JsonTypeMapper
        val gson = Gson()
        val gsonMapper = GsonJsonTypeMapper(gson, Types.VARCHAR)

        // Test field detection
        assertTrue(gsonMapper.isJsonField(attributesField))
        assertTrue(gsonMapper.isJsonField(tagsField))
        assertTrue(gsonMapper.isJsonField(metadataField))
        assertFalse(gsonMapper.isJsonField(createdAtField))

        // Test SQL type determination
        assertEquals(Types.VARCHAR, gsonMapper.getSqlTypeForField(attributesField))
        assertEquals(Types.VARCHAR, gsonMapper.getSqlTypeForField(tagsField))
        assertEquals(Types.VARCHAR, gsonMapper.getSqlTypeForField(metadataField))

        // Test JSON serialization
        val jsonAttributes = gsonMapper.toJson(testEntity.attributes)
        assertTrue(jsonAttributes.contains("key1"))
        assertTrue(jsonAttributes.contains("value1"))

        val jsonMetadata = gsonMapper.toJson(testEntity.metadata)
        assertTrue(jsonMetadata.contains("version"))
        assertTrue(jsonMetadata.contains("1.0.0"))
        assertTrue(jsonMetadata.contains("nested"))
        assertTrue(jsonMetadata.contains("42.5"))

        // Test value handling
        assertFalse(gsonMapper.canHandleValue(null))
        assertTrue(gsonMapper.canHandleValue(testEntity.attributes))
        assertTrue(gsonMapper.canHandleValue(testEntity.metadata))
        assertFalse(gsonMapper.canHandleValue(testEntity.createdAt))
    }

    /**
     * Test the JSON-B mapper implementation.
     * Note: This test requires the JSON-B implementation on the classpath.
     */
    @Test
    fun testJsonbMapper() {
        try {
            // Create Jsonb and JsonTypeMapper
            val jsonb = JsonbBuilder.create()
            val jsonbMapper = JsonbJsonTypeMapper(jsonb, Types.VARCHAR)

            // Test field detection
            assertTrue(jsonbMapper.isJsonField(attributesField))
            assertTrue(jsonbMapper.isJsonField(tagsField))
            assertTrue(jsonbMapper.isJsonField(metadataField))
            assertFalse(jsonbMapper.isJsonField(createdAtField))

            // Test SQL type determination
            assertEquals(Types.VARCHAR, jsonbMapper.getSqlTypeForField(attributesField))
            assertEquals(Types.VARCHAR, jsonbMapper.getSqlTypeForField(tagsField))
            assertEquals(Types.VARCHAR, jsonbMapper.getSqlTypeForField(metadataField))
            assertNotEquals(Types.VARCHAR, jsonbMapper.getSqlTypeForField(createdAtField))

            // Test JSON serialization
            val jsonAttributes = jsonbMapper.toJson(testEntity.attributes)
            assertTrue(jsonAttributes.contains("key1"))
            assertTrue(jsonAttributes.contains("value1"))

            val jsonMetadata = jsonbMapper.toJson(testEntity.metadata)
            assertTrue(jsonMetadata.contains("version"))
            assertTrue(jsonMetadata.contains("1.0.0"))

            // Test value handling
            assertFalse(jsonbMapper.canHandleValue(null))
            assertTrue(jsonbMapper.canHandleValue(testEntity.attributes))
            assertTrue(jsonbMapper.canHandleValue(testEntity.metadata))
            assertFalse(jsonbMapper.canHandleValue(testEntity.createdAt))
        } catch (e: NoClassDefFoundError) {
            // Skip test if JSON-B is not on the classpath
            System.out.println("Skipping JSON-B test as it's not on the classpath")
        }
    }

    /**
     * Test registering and using a mapper through the TypeMapperRegistry.
     */
    @Test
    fun testTypeMapperRegistryWithJsonMapper() {
        // Clear existing mappers and register a Jackson mapper

        // Create and register Jackson mapper
        val objectMapper = ObjectMapper().registerKotlinModule()
        val jacksonMapper = JacksonJsonTypeMapper(objectMapper, Types.VARCHAR)

        val typeMapperRegistry = TypeMapperRegistry(testTypeProvider(jacksonMapper))

        // Test that the registry uses the Jackson mapper
        assertEquals(Types.VARCHAR, typeMapperRegistry.getSqlTypeForField(attributesField))
        assertEquals(Types.VARCHAR, typeMapperRegistry.getSqlTypeForField(tagsField))
        assertEquals(Types.VARCHAR, typeMapperRegistry.getSqlTypeForField(metadataField))
        assertNotEquals(Types.VARCHAR, typeMapperRegistry.getSqlTypeForField(createdAtField))

        // Test conversion through registry
        val jsonValue = typeMapperRegistry.convertToJdbcValue(testEntity.metadata, metadataField)
        assertTrue(jsonValue is String)
        assertTrue((jsonValue as String).contains("version"))
        assertTrue(jsonValue.contains("1.0.0"))

    }
}
