package io.github.mpecan.upsert.type.json

import com.google.gson.Gson
import io.github.mpecan.upsert.type.AbstractJsonTypeMapper

/**
 * Implementation of JsonTypeMapper using Gson.
 * Uses Google's Gson library for JSON serialization.
 */
class GsonJsonTypeMapper(private val gson: Gson, sqlType: Int) : AbstractJsonTypeMapper(sqlType) {

    /**
     * Serialize an object to its JSON string representation using Gson.
     *
     * @param value The value to serialize
     * @return The JSON string representation
     */
    override fun toJson(value: Any): String {
        return gson.toJson(value)
    }
}