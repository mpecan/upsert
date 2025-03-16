package si.pecan.upsert.annotation

import java.lang.annotation.Inherited

/**
 * Annotation to mark a repository method as an upsert operation.
 * This will generate the appropriate SQL for either PostgreSQL or MySQL
 * based on the database being used.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@Inherited
annotation class Upsert

/**
 * Annotation to mark a field as a key for the upsert operation.
 * Fields marked with this annotation will be used in the WHERE clause
 * to determine if a record exists.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@Inherited
annotation class UpsertKey

/**
 * Annotation to mark a field as a value to be updated during the upsert operation.
 * Fields marked with this annotation will be updated if the record exists,
 * or inserted along with the key fields if the record doesn't exist.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@Inherited
annotation class UpsertValue
