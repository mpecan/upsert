package io.github.mpecan.upsert.type.json

import io.github.mpecan.upsert.type.AbstractJsonTypeMapper
import jakarta.json.bind.Jsonb

/**
 * Implementation of JsonTypeMapper using JSON-B.
 * Uses Jakarta JSON Binding (JSON-B) for JSON serialization.
 */
class JsonbJsonTypeMapper(private val jsonb: Jsonb, sqlType: Int) :
    AbstractJsonTypeMapper(sqlType) {

    /**
     * Serialize an object to its JSON string representation using JSON-B.
     *
     * @param value The value to serialize
     * @return The JSON string representation
     */
    override fun toJson(value: Any): String {
        return jsonb.toJson(value)
    }
}