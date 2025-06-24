import com.vanniktech.maven.publish.SonatypeHost
import java.time.Duration

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.spring") version "2.1.21"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("java-library")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.32.0"
    id("org.sonarqube") version "6.2.0.5505"
    id("jacoco")
}

group = "io.github.mpecan"
version = project.property("version").toString()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

val sqliteVersion = "3.50.1.0"
val mockitoKotlinVersion = "5.4.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Database drivers
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.xerial:sqlite-jdbc:$sqliteVersion")
    runtimeOnly("org.hibernate.orm:hibernate-community-dialects")

    compileOnly("com.fasterxml.jackson.core:jackson-databind")
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    compileOnly("com.google.code.gson:gson")
    compileOnly("jakarta.json.bind:jakarta.json.bind-api")
    compileOnly("org.eclipse:yasson") // JSON-B implementation

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.platform:junit-platform-launcher")

    // Test containers for integration testing
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:mysql")

    // Testing of the JSON type mappers
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("com.google.code.gson:gson")
    testImplementation("jakarta.json.bind:jakarta.json.bind-api")
    testImplementation("org.eclipse:yasson") // JSON-B implementation
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Enable reusing TestContainers instances across test runs
    systemProperty("testcontainers.reuse.enable", "true")

    // Set lower values for faster test execution - particularly when stopping tests
    systemProperty("spring.datasource.hikari.maximum-pool-size", "3")
    systemProperty("spring.datasource.hikari.minimum-idle", "1")
    systemProperty("spring.datasource.hikari.connection-timeout", "5000")
    systemProperty("spring.datasource.hikari.idle-timeout", "2000")
    systemProperty("spring.datasource.hikari.max-lifetime", "5000")
    systemProperty("spring.datasource.hikari.shutdown-timeout", "1000")
}

tasks.test {
    useJUnitPlatform {
        excludeTags("performance")
    }

    testLogging {
        events("passed", "skipped", "failed")
        
        // Only show output streams for failed tests
        showStandardStreams = false
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    
    // Generate detailed test reports
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
    
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("performanceTest") {
    description = "Runs performance tests comparing upsert operations with Spring Data JPA saveAll operations"
    group = "verification"
    
    useJUnitPlatform {
        includeTags("performance")
    }

    // Performance tests need more time
    timeout.set(Duration.ofMinutes(30))
    
    // Enable reusing TestContainers instances across test runs for performance tests
    systemProperty("testcontainers.reuse.enable", "true")
    
    // Set higher pool sizes for performance testing
    systemProperty("spring.datasource.hikari.maximum-pool-size", "10")
    systemProperty("spring.datasource.hikari.minimum-idle", "5")
    systemProperty("spring.datasource.hikari.connection-timeout", "30000")
    systemProperty("spring.datasource.hikari.idle-timeout", "60000")
    systemProperty("spring.datasource.hikari.max-lifetime", "300000")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    
    // Ensure performance tests generate reports
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
    
    doFirst {
        logger.lifecycle("Running performance tests - this may take several minutes...")
        logger.lifecycle("Performance reports will be generated at: ${reports.html.outputLocation.get()}")
    }
}

// Task to run performance tests for MySQL only
tasks.register<Test>("performanceTestMySql") {
    description = "Runs performance tests for MySQL only"
    group = "verification"
    
    useJUnitPlatform {
        includeTags("performance")
        includeEngines("junit-jupiter")
        includeEngines("junit-vintage")
    }
    
    filter {
        includeTestsMatching("*MySqlPerformanceTest*")
    }
    
    // Inherit all settings from performanceTest
    timeout.set(Duration.ofMinutes(30))
    systemProperty("testcontainers.reuse.enable", "true")
    systemProperty("spring.datasource.hikari.maximum-pool-size", "10")
    systemProperty("spring.datasource.hikari.minimum-idle", "5")
    systemProperty("spring.datasource.hikari.connection-timeout", "30000")
    systemProperty("spring.datasource.hikari.idle-timeout", "60000")
    systemProperty("spring.datasource.hikari.max-lifetime", "300000")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    
    doFirst {
        logger.lifecycle("Running MySQL performance tests only...")
    }
}

// Task to run performance tests for PostgreSQL only
tasks.register<Test>("performanceTestPostgreSql") {
    description = "Runs performance tests for PostgreSQL only"
    group = "verification"
    
    useJUnitPlatform {
        includeTags("performance")
        includeEngines("junit-jupiter")
        includeEngines("junit-vintage")
    }
    
    filter {
        includeTestsMatching("*PostgreSqlPerformanceTest*")
    }
    
    // Inherit all settings from performanceTest
    timeout.set(Duration.ofMinutes(30))
    systemProperty("testcontainers.reuse.enable", "true")
    systemProperty("spring.datasource.hikari.maximum-pool-size", "10")
    systemProperty("spring.datasource.hikari.minimum-idle", "5")
    systemProperty("spring.datasource.hikari.connection-timeout", "30000")
    systemProperty("spring.datasource.hikari.idle-timeout", "60000")
    systemProperty("spring.datasource.hikari.max-lifetime", "300000")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    
    doFirst {
        logger.lifecycle("Running PostgreSQL performance tests only...")
    }
}

// Create a source jar for publishing
tasks.register<Jar>("sourceJar") {
    description = "Creates a JAR containing the source code"
    group = "publishing"
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

// Create a javadoc jar for publishing
tasks.register<Jar>("javadocJar") {
    description = "Creates a JAR containing the Javadoc"
    group = "documentation"
    from(tasks.named("javadoc"))
    archiveClassifier.set("javadoc")
}

// Disable Spring Boot's executable jar
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

// Enable jar task to create a plain jar
tasks.getByName<Jar>("jar") {
    enabled = true
}
mavenPublishing {
    coordinates("io.github.mpecan", "upsert", project.property("version").toString())
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    pom {
        name.set("Spring Data JPA Upsert")
        description.set("A Spring Data JPA extension providing upsert operations")
        url.set("https://github.com/mpecan/upsert")
        inceptionYear.set("2025")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("mpecan")
                name.set("Matjaž Pečan")
                email.set("matjaz.pecan@gmail.com")
                organization.set("Pecan")
                organizationUrl.set("https://github.com/mpecan")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/mpecan/upsert.git")
            developerConnection.set("scm:git:ssh://github.com/mpecan/upsert.git")
            url.set("https://github.com/mpecan/upsert")
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "mpecan_upsert")
        property("sonar.organization", "mpecan")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
