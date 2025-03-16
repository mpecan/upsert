package si.pecan.upsert.repository

import org.springframework.context.ApplicationContext
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.RepositoryComposition
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.repository.core.support.RepositoryFragment
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider
import java.io.Serializable
import java.util.Optional
import javax.persistence.EntityManager

/**
 * Factory bean for creating repositories with upsert capabilities.
 * This factory bean creates repositories that include the UpsertRepositoryImpl functionality.
 *
 * @param T The repository type
 * @param S The entity type
 * @param ID The type of the entity's ID
 */
class UpsertRepositoryFactoryBean<T : Repository<S, ID>, S : Any, ID : Serializable>(
    repositoryInterface: Class<T>,
    private val applicationContext: ApplicationContext
) : JpaRepositoryFactoryBean<T, S, ID>(repositoryInterface) {

    override fun createRepositoryFactory(entityManager: EntityManager): RepositoryFactorySupport {
        return UpsertRepositoryFactory(entityManager, applicationContext)
    }

    /**
     * Factory for creating repositories with upsert capabilities.
     */
    private class UpsertRepositoryFactory(
        entityManager: EntityManager,
        private val applicationContext: ApplicationContext
    ) : JpaRepositoryFactory(entityManager) {

        private val em = entityManager
        private lateinit var repository: UpsertRepository<Any, Any>

        override fun getRepositoryFragments(metadata: RepositoryMetadata): RepositoryComposition.RepositoryFragments {
            val fragments = super.getRepositoryFragments(metadata)

            // If the repository extends UpsertRepository, add the UpsertRepositoryImpl fragment
            if (UpsertRepository::class.java.isAssignableFrom(metadata.repositoryInterface)) {
                val upsertRepositoryImpl = UpsertRepositoryImpl<Any, Any>(metadata)
                applicationContext.autowireCapableBeanFactory.autowireBean(upsertRepositoryImpl)

                val upsertFragment = RepositoryFragment.implemented(
                    UpsertRepository::class.java,
                    upsertRepositoryImpl
                )
                repository = upsertRepositoryImpl

                return fragments.append(upsertFragment)
            }

            return fragments
        }

        override fun getQueryLookupStrategy(
            key: QueryLookupStrategy.Key?,
            evaluationContextProvider: QueryMethodEvaluationContextProvider
        ): Optional<QueryLookupStrategy> {
            // Get the standard query lookup strategy
            val delegateStrategy = super.getQueryLookupStrategy(key, evaluationContextProvider)

            // Wrap it with our custom strategy
            return Optional.of(UpsertQueryLookupStrategy(delegateStrategy, repository))
        }
    }
}
