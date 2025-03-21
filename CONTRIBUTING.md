# Contributing to Upsert Repository

Thank you for considering contributing to the Upsert Repository project! This document provides
guidelines and instructions for contributing to the project.

## Table of Contents

- [Deploying to Maven Central](#deploying-to-maven-central)
- [GitHub Actions Deployment](#github-actions-deployment)
    - [Automated Versioning with Release Please](#automated-versioning-with-release-please)
    - [Release Deployment](#release-deployment)
    - [SNAPSHOT Deployment](#snapshot-deployment)

## Deploying to Maven Central

This project uses
the [com.vanniktech.maven.publish](https://github.com/vanniktech/maven-publish-plugin) Gradle plugin
to simplify the process of publishing to Maven Central. If you're a contributor and need to deploy a
new version, follow these steps:

1. **Set up OSSRH Account**: Create an account on [Sonatype OSSRH](https://s01.oss.sonatype.org/).

2. **Configure GPG**:
    - Install GPG
    - Generate a key pair: `gpg --gen-key`
    - List keys: `gpg --list-keys`
    - Distribute your public key: `gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID`

3. **Configure Credentials**:
    - Edit `gradle.properties` in your project root or in your Gradle home directory
    - Add your OSSRH credentials and GPG configuration (see the template in the project's
      `gradle.properties`)
    - You have two options for GPG signing:
        - Option 1: Configure GPG key details explicitly with the three properties
        - Option 2: Use the gpg command-line tool, which will use your default GPG key
    - Alternatively, you can set environment variables for CI/CD environments

4. **Deploy**:
    - Run: `./gradlew publish`
    - The maven-publish plugin will handle building the project, signing the artifacts, and
      uploading them to OSSRH

5. **Release**:
    - With the maven-publish plugin configured to use SonatypeHost.CENTRAL_PORTAL, the release
      process is simplified
    - The plugin handles the staging repository creation, closing, and releasing automatically
    - You can monitor the release status on [Sonatype OSSRH](https://s01.oss.sonatype.org/)

For more detailed instructions on the plugin configuration, see
the [maven-publish-plugin documentation](https://github.com/vanniktech/maven-publish-plugin).

## GitHub Actions Deployment

This project is configured to automatically deploy to Maven Central using GitHub Actions in two
ways:

### Automated Versioning with Release Please

This project uses [Release Please](https://github.com/googleapis/release-please) to automate version
management and release creation. Release Please:

1. Creates and maintains a release PR that:
    - Updates the version in `gradle.properties`
    - Updates the changelog based on conventional commit messages
    - Keeps the PR up-to-date as new commits are pushed

2. When the release PR is merged:
    - A GitHub release is automatically created
    - The release deployment workflow is triggered

To create a new release:

1. Use [conventional commits](https://www.conventionalcommits.org/) in your commit messages:
    - `fix:` for bug fixes (patch version bump)
    - `feat:` for new features (minor version bump)
    - `feat!:` or `fix!:` for breaking changes (major version bump)
    - Include `BREAKING CHANGE:` in the commit body for breaking changes

2. Push your commits to the main branch
    - Release Please will create or update a release PR

3. Review and merge the release PR when ready to release
    - This will trigger the release process automatically

### Release Deployment

When a new release is created (either manually or via Release Please), the project will be deployed
to Maven Central as a release version using the com.vanniktech.maven.publish plugin. To set this up:

1. **Configure GitHub Secrets**:
    - Go to your repository's Settings > Secrets and variables > Actions
    - Add the following secrets:
        - `OSSRH_USERNAME`: Your Sonatype OSSRH username
        - `OSSRH_PASSWORD`: Your Sonatype OSSRH password
        - `GPG_PRIVATE_KEY`: Your GPG private key (export with
          `gpg --export-secret-keys --armor YOUR_KEY_ID`)
        - `GPG_PASSPHRASE`: Your GPG key passphrase
        - `GPG_KEY_ID`: Your GPG key ID (last 8 characters of your key ID)
        - `REPOSITORY_BUTLER_APP_ID` and `REPOSITORY_BUTLER_PEM`: For the GitHub App used by Release
          Please (if applicable)

2. **Monitor Deployment**:
    - The GitHub Actions workflow will automatically trigger when a release is created
    - You can monitor the progress in the Actions tab
    - The workflow uses the com.vanniktech.maven.publish plugin to handle the build, signing, and
      deployment
    - With the plugin configured to use SonatypeHost.CENTRAL_PORTAL, the release process is
      automated

### SNAPSHOT Deployment

The project is also configured to automatically deploy SNAPSHOT versions to Maven Central whenever
there is a push to the main branch and the current version is a SNAPSHOT version. This allows users
to access the latest development version without waiting for an official release.

When a push is made to the main branch:

1. The workflow checks if the current version in gradle.properties already has a "-SNAPSHOT" suffix
2. If it is a SNAPSHOT version, the project will be built and tested
3. The com.vanniktech.maven.publish plugin handles signing the artifacts and deploying them to the
   Maven Central SNAPSHOT repository
4. If it is not a SNAPSHOT version, the workflow will exit without deploying

To use the SNAPSHOT version in your project:

```kotlin
repositories {
    mavenCentral()
    // Add the Maven Central SNAPSHOT repository
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation("io.github.mpecan:upsert:0.0.1-SNAPSHOT")
}
```