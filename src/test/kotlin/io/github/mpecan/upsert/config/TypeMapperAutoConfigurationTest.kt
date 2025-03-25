package io.github.mpecan.upsert.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import io.github.mpecan.upsert.dialect.UpsertDialect
import io.github.mpecan.upsert.type.JsonTypeMapper
import io.github.mpecan.upsert.type.TypeMapper
import io.github.mpecan.upsert.type.TypeMapperRegistry
import io.github.mpecan.upsert.type.json.GsonJsonTypeMapper
import io.github.mpecan.upsert.type.json.JacksonJsonTypeMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Field
import java.sql.Types

/**
 * Tests for JsonTypeMapperAutoConfiguration.
 * Verifies that the appropriate JSON mapper is configured based on available libraries.
 */
class TypeMapperAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TypeMapperAutoConfiguration::class.java))


    /**
     * Test that Jackson mapper is used when Jackson is available.
     */
    @Test
    fun jacksonMapperIsUsedWhenAvailable() {
        contextRunner
            .withUserConfiguration(JacksonConfiguration::class.java)
            .run { context ->

                val typeMapperRegistry = context.getBean(TypeMapperRegistry::class.java)
                // Verify that the context has a JacksonJsonTypeMapper bean
                assertTrue(context.containsBean("jsonTypeMapper"))
                val jsonMapper = context.getBean("jsonTypeMapper")
                assertTrue(jsonMapper is JacksonJsonTypeMapper)

                // Verify it was registered with the TypeMapperRegistry
                val registeredMapper = typeMapperRegistry.getRegisteredMappers()
                    .filterIsInstance<JacksonJsonTypeMapper>()
                    .firstOrNull()

                assertNotNull(registeredMapper)
            }
    }

    /**
     * Test that Gson mapper is used when Gson is available but Jackson is not.
     */
    @Test
    fun gsonMapperIsUsedWhenJacksonIsNotAvailable() {
        contextRunner
            .withUserConfiguration(GsonConfiguration::class.java)
            .run { context ->
                val typeMapperRegistry = context.getBean(TypeMapperRegistry::class.java)
                // Verify that the context has a GsonJsonTypeMapper bean
                assertTrue(context.containsBean("jsonTypeMapper"))
                val jsonMapper = context.getBean("jsonTypeMapper")
                assertTrue(jsonMapper is GsonJsonTypeMapper)

                // Verify it was registered with the TypeMapperRegistry
                val registeredMapper = typeMapperRegistry.getRegisteredMappers()
                    .filterIsInstance<GsonJsonTypeMapper>()
                    .firstOrNull()

                assertNotNull(registeredMapper)
            }
    }

    /**
     * Test that a custom mapper is used when provided.
     */
    @Test
    fun customMapperIsUsedWhenProvided() {
        contextRunner
            .withUserConfiguration(CustomMapperConfiguration::class.java)
            .run { context ->
                val typeMapperRegistry = context.getBean(TypeMapperRegistry::class.java)
                // Verify that the context has a CustomJsonTypeMapper bean
                assertTrue(context.containsBean("jsonTypeMapper"))
                val jsonMapper = context.getBean("jsonTypeMapper")
                assertTrue(jsonMapper is CustomMapperConfiguration.CustomJsonTypeMapper)

                // Verify it was registered with the TypeMapperRegistry
                val registeredMapper = typeMapperRegistry.getRegisteredMappers()
                    .filterIsInstance<CustomMapperConfiguration.CustomJsonTypeMapper>()
                    .firstOrNull()

                assertNotNull(registeredMapper)
            }
    }

    /**
     * Test that Jackson mapper is preferred over Gson when both are available.
     */
    @Test
    fun jacksonMapperIsPreferredOverGsonWhenBothAreAvailable() {
        contextRunner
            .withUserConfiguration(JacksonAndGsonConfiguration::class.java)
            .run { context ->
                val typeMapperRegistry = context.getBean(TypeMapperRegistry::class.java)
                // Verify that the context has a JacksonJsonTypeMapper bean
                assertTrue(context.containsBean("jsonTypeMapper"))
                val jsonMapper = context.getBean("jsonTypeMapper")
                assertTrue(jsonMapper is JacksonJsonTypeMapper)

                // Verify the right mapper was registered with the TypeMapperRegistry
                val jacksonMapper = typeMapperRegistry.getRegisteredMappers()
                    .filterIsInstance<JacksonJsonTypeMapper>()
                    .firstOrNull()

                assertNotNull(jacksonMapper)

                // Verify Gson mapper was not registered
                val gsonMapper = typeMapperRegistry.getRegisteredMappers()
                    .filterIsInstance<GsonJsonTypeMapper>()
                    .firstOrNull()

                assertNull(gsonMapper)
            }
    }

    // Test configurations

    @Configuration
    class JacksonConfiguration {

        @Bean
        fun dialect(): UpsertDialect {
            return mock {
                on { getJsonType() } doReturn Types.VARCHAR
            }
        }

        @Bean
        fun objectMapper(): ObjectMapper {
            return ObjectMapper()
        }

        @Bean
        fun typeMapperRegistry(provider: ObjectProvider<List<TypeMapper>>): TypeMapperRegistry {
            return TypeMapperRegistry(provider)
        }
    }

    @Configuration
    class GsonConfiguration {
        @Bean
        fun dialect(): UpsertDialect {
            return mock {
                on { getJsonType() } doReturn Types.VARCHAR
            }
        }

        @Bean
        fun gson(): Gson {
            return Gson()
        }

        @Bean
        fun typeMapperRegistry(provider: ObjectProvider<List<TypeMapper>>): TypeMapperRegistry {
            return TypeMapperRegistry(provider)
        }
    }

    @Configuration
    class JacksonAndGsonConfiguration {
        @Bean
        fun dialect(): UpsertDialect {
            return mock {
                on { getJsonType() } doReturn Types.VARCHAR
            }
        }

        @Bean
        fun objectMapper(): ObjectMapper {
            return ObjectMapper()
        }

        @Bean
        fun gson(): Gson {
            return Gson()
        }

        @Bean
        fun typeMapperRegistry(provider: ObjectProvider<List<TypeMapper>>): TypeMapperRegistry {
            return TypeMapperRegistry(provider)
        }
    }

    @Configuration
    class CustomMapperConfiguration {

        @Bean
        fun jsonTypeMapper(): JsonTypeMapper {
            return CustomJsonTypeMapper()
        }

        @Bean
        fun typeMapperRegistry(provider: ObjectProvider<List<TypeMapper>>): TypeMapperRegistry {
            return TypeMapperRegistry(provider)

        }

        // Custom JsonTypeMapper for testing
        class CustomJsonTypeMapper : JsonTypeMapper {
            override val sqlType: Int
                get() = Types.VARCHAR

            override fun toJson(value: Any): String = "CUSTOM_JSON"

            override fun canHandle(field: Field): Boolean = true

            override fun canHandleValue(value: Any?): Boolean = true

            override fun convertToJdbcValue(value: Any?): Any = "CUSTOM_CONVERTED"

            override fun getSqlTypeForField(field: Field): Int = Types.VARCHAR
        }


    }
}
