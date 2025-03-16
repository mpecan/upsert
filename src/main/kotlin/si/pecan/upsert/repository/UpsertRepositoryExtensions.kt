package si.pecan.upsert.repository

import org.springframework.data.repository.Repository
import org.springframework.data.repository.findByIdOrNull
import javax.persistence.EntityManager
import javax.persistence.Table

/**
 * Extension functions for repositories to add upsert functionality.
 */

/**
 * Perform an upsert operation for the given entity.
 *
 * @param entity The entity to upsert
 * @param upsertOperations The UpsertOperations bean
 * @param entityManager The EntityManager bean
 * @return The number of rows affected
 */
fun <T : Any, ID> Repository<T, ID>.upsert(
    entity: T,
    upsertOperations: UpsertOperations,
    entityManager: EntityManager
): Int {
    val tableName = getTableName(entity.javaClass, entityManager)
    return upsertOperations.upsert(entity, tableName)
}

/**
 * Perform an upsert operation for the given list of entities.
 *
 * @param entities The list of entities to upsert
 * @param upsertOperations The UpsertOperations bean
 * @param entityManager The EntityManager bean
 * @return The total number of rows affected
 */
fun <T : Any, ID> Repository<T, ID>.upsertAll(
    entities: List<T>,
    upsertOperations: UpsertOperations,
    entityManager: EntityManager
): Int {
    if (entities.isEmpty()) {
        return 0
    }
    
    val tableName = getTableName(entities.first().javaClass, entityManager)
    return upsertOperations.upsertAll(entities, tableName)
}

/**
 * Get the table name for the given entity class.
 *
 * @param entityClass The entity class
 * @param entityManager The EntityManager bean
 * @return The table name
 */
private fun getTableName(entityClass: Class<*>, entityManager: EntityManager): String {
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