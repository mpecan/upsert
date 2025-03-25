package io.github.mpecan.upsert.repository

import io.github.mpecan.upsert.type.TypeMapperRegistry
import jakarta.persistence.EntityManager
import org.springframework.context.ApplicationContext
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.io.Serializable
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
    private val typeMapperRegistry: TypeMapperRegistry
) : JpaRepositoryFactoryBean<T, S, ID>(repositoryInterface) {

    override fun createRepositoryFactory(entityManager: EntityManager): RepositoryFactorySupport {
        return UpsertRepositoryFactory(
            entityManager,
            applicationContext,
            dataSource,
            jdbcTemplate,
            typeMapperRegistry
        )
    }
}

