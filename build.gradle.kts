plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
    id("signing")
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


dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Database drivers
    implementation("org.postgresql:postgresql")
    implementation("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Test containers for integration testing
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:mysql")
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

// Create a source jar for publishing
tasks.register<Jar>("sourceJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

// Create a javadoc jar for publishing
tasks.register<Jar>("javadocJar") {
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            artifact(tasks["sourceJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("Spring Data JPA Upsert")
                description.set("A Spring Data JPA extension providing upsert operations")
                url.set("https://github.com/mpecan/upsert")

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
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://central.sonatype.com/repository/maven/ ")
            credentials {
                username = project.findProperty("ossrhUsername") as String?
                    ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String?
                    ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    // Check if GPG key details are provided
    val signingKeyId =
        project.findProperty("signing.keyId") as String? ?: System.getenv("SIGNING_KEY_ID")
    val signingPassword =
        project.findProperty("signing.password") as String? ?: System.getenv("SIGNING_PASSWORD")
    val signingSecretKeyRingFile = project.findProperty("signing.secretKeyRingFile") as String?
        ?: System.getenv("SIGNING_SECRET_KEY_RING_FILE")

    // If all signing properties are provided, use them
    if (signingKeyId != null && signingPassword != null && signingSecretKeyRingFile != null) {
        useInMemoryPgpKeys(signingKeyId, File(signingSecretKeyRingFile).readText(), signingPassword)
    } else {
        // Otherwise, try to use gpg command
        useGpgCmd()
    }

    // Only sign when publishing to Maven Central
    setRequired { gradle.taskGraph.hasTask("publishToSonatype") || gradle.taskGraph.hasTask("publishToMavenLocal") }

    sign(publishing.publications["maven"])
}

// Task to generate checksums for Maven Central
tasks.register("generateChecksums") {
    description = "Generates checksums for Maven Central"
    group = "publishing"

    doLast {
        val publishTask = tasks.named("publishMavenPublicationToOSSRHRepository").get()
        val publishDir = publishTask.outputs.files.singleFile

        fileTree(publishDir) {
            include("**/*.jar")
            include("**/*.pom")
        }.forEach { file ->
            // Generate MD5 checksum
            ant.withGroovyBuilder {
                "checksum"("file" to file, "algorithm" to "MD5", "fileext" to ".md5")
            }

            // Generate SHA-1 checksum
            ant.withGroovyBuilder {
                "checksum"("file" to file, "algorithm" to "SHA-1", "fileext" to ".sha1")
            }

            // Generate SHA-256 checksum (optional)
            ant.withGroovyBuilder {
                "checksum"("file" to file, "algorithm" to "SHA-256", "fileext" to ".sha256")
            }
        }
    }
}

// Make publish task depend on generateChecksums
tasks.named("publish") {
    finalizedBy("generateChecksums")
}

// Task to print the project version (for testing purposes)
tasks.register("printVersion") {
    doLast {
        println("Project version: ${project.version}")
    }
}
