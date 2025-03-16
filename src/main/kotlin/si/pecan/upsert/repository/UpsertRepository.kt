package si.pecan.upsert.repository

import org.springframework.data.repository.Repository
import org.springframework.data.repository.NoRepositoryBean

/**
 * Repository interface with upsert capabilities.
 * This interface extends Spring Data's Repository interface and adds upsert functionality.
 *
 * @param T The entity type
 * @param ID The type of the entity's ID
 */
@NoRepositoryBean
interface UpsertRepository<T : Any, ID> : Repository<T, ID> {
    
    /**
     * Perform an upsert operation for the given entity.
     *
     * @param entity The entity to upsert
     * @return The number of rows affected
     */
    fun upsert(entity: T): Int
    
    /**
     * Perform an upsert operation for the given list of entities.
     *
     * @param entities The list of entities to upsert
     * @return The total number of rows affected
     */
    fun upsertAll(entities: List<T>): Int
}