package si.pecan.upsert.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.NoRepositoryBean
import javax.persistence.EntityManager
import javax.persistence.Table

/**
 * Implementation of UpsertRepository.
 * This class provides the implementation for the upsert and upsertAll methods.
 *
 * @param T The entity type
 * @param ID The type of the entity's ID
 */
class UpsertRepositoryImpl<T : Any, ID> (

): UpsertRepository<T, ID> {

    @Autowired
    private lateinit var upsertOperations: UpsertOperations

    @Autowired
    private lateinit var entityManager: EntityManager

    /**
     * Perform an upsert operation for the given entity.
     *
     * @param entity The entity to upsert
     * @return The number of rows affected
     */
    override fun upsert(entity: T): Int {
        val tableName = getTableName(entity.javaClass)
        return upsertOperations.upsert(entity, tableName)
    }

    /**
     * Perform an upsert operation for the given list of entities.
     *
     * @param entities The list of entities to upsert
     * @return The total number of rows affected
     */
    override fun upsertAll(entities: List<T>): Int {
        if (entities.isEmpty()) {
            return 0
        }

        val tableName = getTableName(entities.first().javaClass)
        return upsertOperations.upsertAll(entities, tableName)
    }

    /**
     * Get the table name for the given entity class.
     *
     * @param entityClass The entity class
     * @return The table name
     */
    private fun getTableName(entityClass: Class<*>): String {
        // Try to get the table name from @Table annotation
        val tableAnnotation = entityClass.getAnnotation(Table::class.java)
        if (tableAnnotation != null && tableAnnotation.name.isNotBlank()) {
            return tableAnnotation.name
        }

        // Try to get the table name from JPA entity metadata
        val entities = entityManager.metamodel.entities
        for (entity in entities) {
            if (entity.javaType == entityClass) {
                return entity.name.lowercase()
            }
        }

        // Fallback to class name
        return entityClass.simpleName.lowercase()
    }
}