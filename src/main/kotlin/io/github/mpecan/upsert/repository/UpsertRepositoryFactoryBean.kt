package io.github.mpecan.upsert.repository

import io.github.mpecan.upsert.dialect.UpsertDialectFactory
import io.github.mpecan.upsert.model.JpaUpsertModelMetadataProvider
import io.github.mpecan.upsert.model.UpsertModel
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.springframework.context.ApplicationContext
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.RepositoryComposition
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.repository.core.support.RepositoryFragment
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.ValueExpressionDelegate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.io.Serializable
import java.util.*
import javax.sql.DataSource

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
    private val applicationContext: ApplicationContext,
    private val dataSource: DataSource,
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : JpaRepositoryFactoryBean<T, S, ID>(repositoryInterface) {

    override fun createRepositoryFactory(entityManager: EntityManager): RepositoryFactorySupport {
        return UpsertRepositoryFactory(
            entityManager,
            applicationContext,
            dataSource,
            jdbcTemplate
        )
    }

    /**
     * Factory for creating repositories with upsert capabilities.
     */
    private class UpsertRepositoryFactory(
        private val entityManager: EntityManager,
        applicationContext: ApplicationContext,
        dataSource: DataSource,
        private val jdbcTemplate: NamedParameterJdbcTemplate
    ) : JpaRepositoryFactory(entityManager) {

        private val upsertDialect = UpsertDialectFactory(dataSource).getDialect()
        private val entityManagerFactory = applicationContext.getBean(EntityManagerFactory::class.java)
        private lateinit var repository: UpsertRepository<Any, Any>

        override fun getRepositoryFragments(metadata: RepositoryMetadata): RepositoryComposition.RepositoryFragments {
            val fragments = super.getRepositoryFragments(metadata)

            // If the repository extends UpsertRepository, add the UpsertRepositoryImpl fragment
            if (UpsertRepository::class.java.isAssignableFrom(
                    metadata.repositoryInterface
                )
            ) {
                val metadataProvider = JpaUpsertModelMetadataProvider(entityManager.metamodel, entityManagerFactory.persistenceUnitUtil, metadata.domainType)
                val upsertModel = UpsertModel(metadataProvider)

                val upsertOperations = JdbcUpsertOperations(
                    jdbcTemplate,
                    upsertDialect,
                    upsertModel
                )
                val upsertRepositoryImpl =
                    UpsertRepositoryImpl<Any, Any>(
                        upsertOperations,
                        upsertModel
                    )
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
            valueExpressionDelegate: ValueExpressionDelegate
        ): Optional<QueryLookupStrategy> {
            // Get the standard query lookup strategy
            val delegateStrategy = super.getQueryLookupStrategy(key, valueExpressionDelegate)

            // Wrap it with our custom strategy
            return Optional.of(
                UpsertQueryLookupStrategy(
                    delegateStrategy,
                    repository
                )
            )
        }
    }
}
