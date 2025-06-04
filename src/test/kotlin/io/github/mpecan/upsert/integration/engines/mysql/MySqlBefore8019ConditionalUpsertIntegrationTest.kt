package io.github.mpecan.upsert.integration.engines.mysql

import io.github.mpecan.upsert.integration.base.AbstractConditionalUpsertIntegrationTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

/**
 * MySQL integration tests for conditional upsert operations on MySQL versions before 8.0.19.
 * These tests use MySQL 8.0.12 which uses the legacy VALUES() syntax with known limitations.
 * 
 * Note: These tests may fail or show unexpected behavior due to MySQL VALUES() function 
 * limitations in multi-column conditional updates. This is expected and documented.
 * The version field update issue in conditional upserts is a known MySQL limitation.
 * 
 * These tests are automatically disabled on ARM64 architecture (Apple Silicon, ARM64 Linux)
 * since MySQL 8.0.12 Docker images are not available for ARM64. They will run on AMD64/x86_64
 * systems including CI environments.
 */
@EnabledIf("io.github.mpecan.upsert.integration.engines.mysql.MySqlBefore8019ConditionalUpsertIntegrationTest#isDockerImageAvailable")
class MySqlBefore8019ConditionalUpsertIntegrationTest : AbstractConditionalUpsertIntegrationTest() {

    companion object {
        @Container
        @JvmStatic
        val mysqlContainer = MySQLContainer(DockerImageName.parse("mysql:8.0.12"))
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl)
            registry.add("spring.datasource.username", mysqlContainer::getUsername)
            registry.add("spring.datasource.password", mysqlContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create" }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.MySQLDialect" }
        }

        /**
         * Check if the Docker image is available for the current system architecture.
         * MySQL 8.0.12 is only available for AMD64/x86_64, not for ARM64 (Apple Silicon).
         */
        @JvmStatic
        fun isDockerImageAvailable(): Boolean {
            val arch = System.getProperty("os.arch").lowercase()
            val isArm = arch.contains("aarch64") || arch.contains("arm64")
            
            if (isArm) {
                println("Skipping MySqlBefore8019ConditionalUpsertIntegrationTest: MySQL 8.0.12 Docker image not available for ARM64 architecture ($arch)")
                return false
            }
            
            return true
        }
        
        @BeforeAll
        @JvmStatic
        fun checkArchitecture() {
            if (!isDockerImageAvailable()) {
                println("Tests will be skipped due to incompatible architecture")
            }
        }
    }

    // Override all test methods to ensure they're only run when the condition is met
    @Test
    @EnabledIf("isDockerImageAvailable")
    override fun `test simple version update without condition`() {
        super.`test simple version update without condition`()
    }

    @Test
    @EnabledIf("isDockerImageAvailable")
    override fun `should update only when updatedAt is more recent using More operator`() {
        super.`should update only when updatedAt is more recent using More operator`()
    }

    @Test
    @EnabledIf("isDockerImageAvailable")
    override fun `should update only when version is greater or equal using MoreOrEqual operator`() {
        super.`should update only when version is greater or equal using MoreOrEqual operator`()
    }

    @Test
    @EnabledIf("isDockerImageAvailable")
    override fun `should update only when price is less using Less operator`() {
        super.`should update only when price is less using Less operator`()
    }

    @Test
    @EnabledIf("isDockerImageAvailable")
    override fun `should update only when score is less or equal using LessOrEqual operator`() {
        super.`should update only when score is less or equal using LessOrEqual operator`()
    }

    @Test
    @EnabledIf("isDockerImageAvailable")
    override fun `should handle conditional upsert with ignoring fields`() {
        super.`should handle conditional upsert with ignoring fields`()
    }

    @Test
    @EnabledIf("isDockerImageAvailable")
    override fun `should handle batch conditional upserts`() {
        super.`should handle batch conditional upserts`()
    }

    @Test
    @EnabledIf("isDockerImageAvailable")
    override fun `should handle complex conditional upserts with multiple conditions`() {
        super.`should handle complex conditional upserts with multiple conditions`()
    }

    @Test
    @EnabledIf("isDockerImageAvailable")
    override fun `should insert new entities regardless of conditional clause`() {
        super.`should insert new entities regardless of conditional clause`()
    }
}