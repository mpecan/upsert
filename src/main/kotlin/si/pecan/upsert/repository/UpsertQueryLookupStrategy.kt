package si.pecan.upsert.repository

import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.NamedQueries
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.RepositoryQuery
import java.lang.reflect.Method
import java.util.Optional
import javax.persistence.EntityManager

/**
 * Custom query lookup strategy to handle upsert* methods.
 * This class intercepts method calls and checks if they start with "upsert".
 */
class UpsertQueryLookupStrategy(
    private val delegateStrategy: Optional<QueryLookupStrategy>,
    private val em: EntityManager,
    private val repository: UpsertRepository<Any, Any>
) : QueryLookupStrategy {

    private val methodNameParser = UpsertMethodNameParser()

    override fun resolveQuery(
        method: Method,
        metadata: RepositoryMetadata,
        factory: ProjectionFactory,
        namedQueries: NamedQueries
    ): RepositoryQuery {
        // If the method name starts with "upsert", handle it with custom logic
        if (method.name.startsWith("upsert") && methodNameParser.parse(method.name) != null) {
            return UpsertRepositoryQuery(method, metadata, em, repository, factory)
        }

        // Otherwise delegate to the standard strategy
        return delegateStrategy.map { strategy ->
            strategy.resolveQuery(method, metadata, factory, namedQueries)
        }.orElseThrow { IllegalStateException("No query lookup strategy available") }
    }
}