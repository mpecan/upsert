package io.github.mpecan.upsert.config

import io.github.mpecan.upsert.repository.UpsertRepositoryFactoryBean
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

class UpsertJpaRepositoriesRegistrar : ImportBeanDefinitionRegistrar {

    override fun registerBeanDefinitions(
        importingClassMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry
    ) {
        // Check if JPA repositories are already configured
        if (isRepositoriesConfigured(registry)) {
            enhanceExistingRepositories(registry)
            return
        }

        // Register our factory bean for JPA repositories
        val repositoriesRegistrarClass =
            Class.forName("org.springframework.data.jpa.repository.config.JpaRepositoriesRegistrar")
        val repositoriesRegistrar = repositoriesRegistrarClass.getDeclaredConstructor()
            .newInstance() as ImportBeanDefinitionRegistrar

        // Create annotation metadata with our factory bean class
        val attributes = AnnotationAttributes()
        attributes.put("repositoryFactoryBeanClass", UpsertRepositoryFactoryBean::class.java.name)

        // Use reflection to set the attributes
        val metadataClass =
            Class.forName("org.springframework.core.type.StandardAnnotationMetadata")
        val metadataConstructor =
            metadataClass.getDeclaredConstructor(Class::class.java, Map::class.java)
        metadataConstructor.isAccessible = true
        val metadata = metadataConstructor.newInstance(
            EnableJpaRepositories::class.java,
            mapOf("value" to attributes)
        )

        // Register repositories with our factory bean
        repositoriesRegistrar.registerBeanDefinitions(metadata as AnnotationMetadata, registry)
    }

    private fun isRepositoriesConfigured(registry: BeanDefinitionRegistry): Boolean {
        // Check if JPA repositories have already been configured
        return registry.containsBeanDefinition("jpaMappingContext") ||
                registry.containsBeanDefinition("jpaContext")
    }

    private fun enhanceExistingRepositories(registry: BeanDefinitionRegistry) {
        // Find existing repository factory beans and update them
        for (beanName in registry.beanDefinitionNames) {
            val beanDefinition = registry.getBeanDefinition(beanName)
            val beanClassName = beanDefinition.beanClassName

            if (beanClassName?.contains("RepositoryFactoryBean") == true) {
                // Replace with our factory bean
                beanDefinition.beanClassName = UpsertRepositoryFactoryBean::class.java.name
            }
        }
    }
}