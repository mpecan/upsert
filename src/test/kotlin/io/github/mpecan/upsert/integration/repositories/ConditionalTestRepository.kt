package io.github.mpecan.upsert.integration.repositories

import io.github.mpecan.upsert.entity.ConditionalTestEntity
import io.github.mpecan.upsert.repository.UpsertRepository

/**
 * Test repository for ConditionalTestEntity with conditional upsert methods.
 * Tests various comparison operators and combinations.
 */
interface ConditionalTestRepository : UpsertRepository<ConditionalTestEntity, Long> {

    // Test More operator
    fun upsertOnIdWhenUpdatedAtMore(entity: ConditionalTestEntity): ConditionalTestEntity
    
    // Test MoreOrEqual operator
    fun upsertOnIdWhenVersionMoreOrEqual(entity: ConditionalTestEntity): ConditionalTestEntity
    
    // Test Less operator
    fun upsertOnIdWhenPriceLess(entity: ConditionalTestEntity): ConditionalTestEntity
    
    // Test LessOrEqual operator
    fun upsertOnIdWhenScoreLessOrEqual(entity: ConditionalTestEntity): ConditionalTestEntity
    
    // Test combinations with Ignoring
    fun upsertOnIdWhenVersionMoreIgnoringDescription(entity: ConditionalTestEntity): ConditionalTestEntity
    
    // Test batch operations with conditions
    fun upsertAllOnIdWhenUpdatedAtMore(entities: List<ConditionalTestEntity>): List<ConditionalTestEntity>
    
    // Test complex combinations
    fun upsertOnNameAndVersionWhenUpdatedAtMoreOrEqualIgnoringActive(entity: ConditionalTestEntity): ConditionalTestEntity
}