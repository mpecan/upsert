package si.pecan.upsert.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * Test entity using JPA annotations for upsert operations.
 */
@Entity
@Table(name = "jpa_test_entity")
data class JpaTestEntity(
    @Id
    val id: Long,
    
    val name: String,
    
    val description: String? = null,
    
    val active: Boolean = true
)