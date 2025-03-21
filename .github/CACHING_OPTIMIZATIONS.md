# GitHub Actions Caching Optimizations

This document describes the caching optimizations implemented in the GitHub Actions workflows for
this project.

## Overview of Changes

The following caching optimizations have been implemented in the GitHub Actions workflows:

1. **Gradle Wrapper Cache**: Caches the Gradle Wrapper files to avoid downloading them on each run.
2. **Gradle Dependencies Cache**: Caches the Gradle dependencies to avoid downloading them on each
   run.
3. **TestContainers Cache**: Caches the TestContainers images to avoid downloading them on each run.
4. **Maven Local Repository Cache**: Caches the Maven local repository for publishing workflows.
5. **Gradle Build Cache**: Enabled the Gradle build cache to reuse task outputs from previous
   builds.

## Workflows Updated

The following workflows have been updated with caching optimizations:

1. **CI Workflow** (`ci.yml`): Used for continuous integration on pushes to main and pull requests.
2. **Release Deployment Workflow** (`release-deployment.yml`): Used for deploying releases.
3. **Snapshot Deployment Workflow** (`snapshot-deployment.yml`): Used for deploying snapshot
   versions.

## Caching Strategies Implemented

### 1. Gradle Wrapper Cache

```yaml
- name: Cache Gradle Wrapper
  uses: actions/cache@v4
  with:
    path: ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-wrapper-${{ hashFiles('**/gradle/wrapper/gradle-wrapper.properties') }}
    restore-keys: |
      ${{ runner.os }}-gradle-wrapper-
```

This cache stores the Gradle Wrapper files, which are used to ensure consistent Gradle versions
across different environments.

### 2. Gradle Dependencies Cache

```yaml
- name: Cache Gradle dependencies
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/jdks
    key: ${{ runner.os }}-gradle-caches-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    restore-keys: |
      ${{ runner.os }}-gradle-caches-
```

This cache stores the Gradle dependencies, which can be quite large and time-consuming to download.

### 3. TestContainers Cache

```yaml
- name: Cache TestContainers images
  uses: actions/cache@v4
  with:
    path: ~/.testcontainers
    key: ${{ runner.os }}-testcontainers-${{ hashFiles('**/build.gradle.kts') }}
    restore-keys: |
      ${{ runner.os }}-testcontainers-
```

This cache stores the TestContainers images, which are used for integration testing and can be large
and time-consuming to download.

### 4. Maven Local Repository Cache

```yaml
- name: Cache Maven local repository
  uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: |
      ${{ runner.os }}-maven-
```

This cache stores the Maven local repository, which is used during the publishing process.

### 5. Gradle Build Cache

The `--build-cache` flag has been added to all Gradle commands to enable the Gradle build cache,
which allows Gradle to reuse task outputs from previous builds.

```yaml
- name: Build with Gradle
  run: ./gradlew build --build-cache

- name: Test
  run: ./gradlew test --build-cache

- name: Publish to Maven Central
  run: ./gradlew publish --build-cache
```

## Benefits

These caching optimizations provide the following benefits:

1. **Faster Builds**: By caching dependencies and build outputs, subsequent builds will be much
   faster.
2. **Reduced Network Usage**: By caching dependencies, less data needs to be downloaded from the
   internet.
3. **Improved Reliability**: By reducing the dependency on external services, builds are less likely
   to fail due to network issues.
4. **Cost Savings**: Faster builds mean less GitHub Actions minutes consumed, potentially reducing
   costs.

## Maintenance

These caching strategies should work well without much maintenance. However, if you encounter any
issues with stale caches, you can:

1. Update the cache keys to invalidate the caches.
2. Manually clear the caches in the GitHub Actions UI.
3. Temporarily disable caching to troubleshoot issues.