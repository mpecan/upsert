package si.pecan.upsert.repository

import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.data.repository.query.QueryMethod
import java.lang.reflect.Method
import javax.persistence.EntityManager

/**
 * Custom repository query to implement the upsert* methods.
 * This class handles the execution of methods with names like "upsertOnNameIgnoringActive".
 */
class UpsertRepositoryQuery(
    private val method: Method,
    private val metadata: RepositoryMetadata,
    private val em: EntityManager,
    private val repository: UpsertRepository<Any, Any>,
    private val factory: ProjectionFactory
) : RepositoryQuery {

    private val methodNameParser = UpsertMethodNameParser()
    private val upsertInfo = methodNameParser.parse(method.name)
        ?: throw IllegalArgumentException("Method ${method.name} is not a valid upsert method")

    override fun execute(parameters: Array<out Any>): Any? {
        // Check for collection parameter (should be first parameter for upsertAll)
        if (parameters.isEmpty()) {
            throw IllegalArgumentException("upsert* methods must have at least one parameter")
        }

        try {
            // Execute the actual upsert logic based on the method name
            return if (upsertInfo.isUpsertAll) {
                // Check if the first parameter is a collection
                if (parameters[0] !is Collection<*>) {
                    throw IllegalArgumentException("upsertAll* methods must have a collection as first parameter")
                }

                @Suppress("UNCHECKED_CAST")
                val entities = parameters[0] as Collection<Any>

                // Call the appropriate upsertAll method
                repository.upsertAll(
                    entities.toList(),
                    upsertInfo.onFields,
                    upsertInfo.ignoredFields,
                    upsertInfo.ignoreAllFields
                )
            } else {
                // Call the appropriate upsert method
                repository.upsert(
                    parameters[0],
                    upsertInfo.onFields,
                    upsertInfo.ignoredFields,
                    upsertInfo.ignoreAllFields
                )
                return 1
            }
        } catch (e: Exception) {
            // Check if the exception is related to using fields without uniqueness or exclusion constraints
            if (e.message?.contains("constraint") == true || e.message?.contains("unique") == true) {
                throw IllegalArgumentException(
                    "Error executing upsert method ${method.name}: " +
                    "The fields specified in the ON clause (${upsertInfo.onFields.joinToString(", ")}) " +
                    "do not have a uniqueness or exclusion constraint. " +
                    "Please use fields with a uniqueness or exclusion constraint for the ON clause.",
                    e
                )
            } else {
                // Re-throw the original exception
                throw e
            }
        }
    }


    override fun getQueryMethod(): QueryMethod {

        return QueryMethod(method, metadata, factory)
    }
}
