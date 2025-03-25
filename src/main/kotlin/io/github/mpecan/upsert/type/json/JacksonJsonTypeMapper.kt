package io.github.mpecan.upsert.type.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.mpecan.upsert.type.AbstractJsonTypeMapper

/**
 * Implementation of JsonTypeMapper using Jackson.
 * Uses Jackson's ObjectMapper for JSON serialization.
 */
class JacksonJsonTypeMapper(private val objectMapper: ObjectMapper, sqlType: Int) :
    AbstractJsonTypeMapper(sqlType) {

    /**
     * Serialize an object to its JSON string representation using Jackson.
     *
     * @param value The value to serialize
     * @return The JSON string representation
     */
    override fun toJson(value: Any): String {
        return objectMapper.writeValueAsString(value)
    }
}