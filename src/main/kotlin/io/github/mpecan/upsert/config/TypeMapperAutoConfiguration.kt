package io.github.mpecan.upsert.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import io.github.mpecan.upsert.dialect.UpsertDialect
import io.github.mpecan.upsert.type.*
import io.github.mpecan.upsert.type.json.GsonJsonTypeMapper
import io.github.mpecan.upsert.type.json.JacksonJsonTypeMapper
import io.github.mpecan.upsert.type.json.JsonbJsonTypeMapper
import jakarta.json.bind.Jsonb
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order

/**
 * Auto-configuration for JSON type mappers.
 * Registers the appropriate JSON mapper based on available libraries.
 */
@AutoConfiguration
class TypeMapperAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    fun typeMapperRegistry(mapperObjectProvider: ObjectProvider<List<TypeMapper>>): TypeMapperRegistry {
        return TypeMapperRegistry(mapperObjectProvider)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultTypeMapper(): TypeMapper {
        return DefaultTypeMapper()
    }

    @Bean
    @ConditionalOnMissingBean
    fun enumTypeMapper(): TypeMapper {
        return EnumTypeMapper()
    }

    @Bean
    @ConditionalOnMissingBean
    fun namedEnumTypeMapper(): TypeMapper {
        return NamedEnumTypeMapper()
    }

    /**
     * Configuration for Jackson-based JSON mapping.
     * This is the preferred JSON mapper and will be used if Jackson is available.
     */
    @Configuration
    @ConditionalOnClass(ObjectMapper::class)
    @Order(1)
    inner class JacksonJsonTypeMapperConfiguration {

        /**
         * Creates and registers a Jackson-based JSON type mapper.
         *
         * @param objectMapper The Jackson ObjectMapper to use for JSON serialization
         * @param dialect The database dialect to determine the appropriate SQL type
         * @return The configured JSON type mapper
         */
        @Bean
        @Primary
        @ConditionalOnBean(ObjectMapper::class)
        fun jsonTypeMapper(
            objectMapper: ObjectMapper,
            dialect: UpsertDialect
        ): JsonTypeMapper {
            return JacksonJsonTypeMapper(objectMapper, dialect.getJsonType())
        }
    }

    /**
     * Configuration for Gson-based JSON mapping.
     * This will be used if Jackson is not available but Gson is.
     */
    @Configuration
    @ConditionalOnClass(Gson::class)
    @ConditionalOnBean(Gson::class)
    @ConditionalOnMissingBean(JsonTypeMapper::class)
    @Order(2)
    inner class GsonJsonTypeMapperConfiguration {

        /**
         * Creates and registers a Gson-based JSON type mapper.
         *
         * @param gson The Gson instance to use for JSON serialization
         * @param dialect The database dialect to determine the appropriate SQL type
         * @return The configured JSON type mapper
         */
        @Bean
        @Primary
        @ConditionalOnMissingBean(JsonTypeMapper::class)
        fun jsonTypeMapper(
            gson: Gson,
            dialect: UpsertDialect
        ): JsonTypeMapper {
            return GsonJsonTypeMapper(gson, dialect.getJsonType())
        }
    }

    /**
     * Configuration for JSON-B based JSON mapping.
     * This will be used if neither Jackson nor Gson is available but JSON-B is.
     */
    @Configuration
    @ConditionalOnClass(Jsonb::class)
    @ConditionalOnBean(Jsonb::class)
    @ConditionalOnMissingBean(JsonTypeMapper::class)
    @Order(3)
    inner class JsonbJsonTypeMapperConfiguration {

        /**
         * Creates and registers a JSON-B based JSON type mapper.
         *
         * @param jsonb The Jsonb instance to use for JSON serialization
         * @param dialect The database dialect to determine the appropriate SQL type
         * @return The configured JSON type mapper
         */
        @Bean
        @Primary
        @ConditionalOnMissingBean(JsonTypeMapper::class)
        fun jsonTypeMapper(
            jsonb: Jsonb,
            dialect: UpsertDialect
        ): JsonTypeMapper {
            return JsonbJsonTypeMapper(jsonb, dialect.getJsonType())
        }
    }
}
