package si.pecan.upsert.entity

import si.pecan.upsert.annotation.UpsertKey
import si.pecan.upsert.annotation.UpsertValue

/**
 * Test entity for upsert operations.
 */
data class TestEntity(
    @UpsertKey
    val id: Long,
    
    @UpsertValue
    val name: String,
    
    @UpsertValue
    val description: String? = null,
    
    @UpsertValue
    val active: Boolean = true
)