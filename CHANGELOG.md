# Changelog

## 1.0.0 (2025-03-21)


### Features

* add custom upsert methods with ON clause and ignored fields support ([48b4148](https://github.com/mpecan/upsert/commit/48b41480b2a407630901ee19f7bd3371da822b7b))
* add HikariCP configuration and teardown methods for integration tests ([e5e6c7a](https://github.com/mpecan/upsert/commit/e5e6c7af65d053dc8f5ff3c9d26abd0ca8b79f48))
* add mise.toml for Node.js version configuration ([ec85b7d](https://github.com/mpecan/upsert/commit/ec85b7d50020fe65714e19497b61aa3063c31f54))
* add MySQL and PostgreSQL upsert implementation documentation ([750421e](https://github.com/mpecan/upsert/commit/750421e5e0b6aa2585801adf2f2220303b83d0f7))
* add MySQL and PostgreSQL upsert implementation documentation ([2c770a7](https://github.com/mpecan/upsert/commit/2c770a79aa751a5e9fb3ff59f36ba0a2fd7d7cca))
* add support for entities with generated IDs in upsert operations and implement integration tests ([8c7cba7](https://github.com/mpecan/upsert/commit/8c7cba7bac1600188d6b20ec23f6cd4ab1934a50))
* add support for JPA custom converters and enhance upsert operations ([08ff8c1](https://github.com/mpecan/upsert/commit/08ff8c191baf16329b0e49f85e16b80597f8db83))
* add UpsertInfo data class and refactor UpsertMethodNameParser to use it ([958075e](https://github.com/mpecan/upsert/commit/958075eb440567490ac286a05a1aa10026f899d3))
* add UpsertInfo data class and refactor UpsertMethodNameParser to use it ([2af3ed1](https://github.com/mpecan/upsert/commit/2af3ed1e0ccf22d00f3b2a5216d3d58d1453151b))
* add upsertOnIdIgnoringAllFields method to allow insert-only operations ([600a4b1](https://github.com/mpecan/upsert/commit/600a4b13c2407e977dacf8c42db6ebefc1fa30e8))
* configure HikariCP settings for improved connection management in tests ([26f2d06](https://github.com/mpecan/upsert/commit/26f2d06844a6f17bae2ad52092e4c8b43b1164ea))
* enhance UpsertModel to improve column handling and validation logic ([7919ad4](https://github.com/mpecan/upsert/commit/7919ad4574fe065152a6dfbd081fe2d8d7f57c13))
* implement abstract JDBC upsert operations with PostgreSQL and MySQL support ([6fe99b9](https://github.com/mpecan/upsert/commit/6fe99b9a4e6a61ae3ac591cdd8ffb227cba4dcb8))
* implement abstract JDBC upsert operations with PostgreSQL and MySQL support ([649d49e](https://github.com/mpecan/upsert/commit/649d49e5d506c07c2deab6cd493a9cc01fb44706))
* implement abstract JDBC upsert operations with PostgreSQL and MySQL support ([2e7dd88](https://github.com/mpecan/upsert/commit/2e7dd88f872a7d55056117e17d18934ab27b3c54))
* implement upsert functionality with support for PostgreSQL and MySQL ([cfad87c](https://github.com/mpecan/upsert/commit/cfad87c238bca203d61541dafb8cdb75661df661))
* implement UpsertModel and related metadata provider for upsert operations ([f04034a](https://github.com/mpecan/upsert/commit/f04034a91190e342378d5871d8d9baafd3d21039))
* implement UpsertModel and related metadata provider for upsert operations ([c9283c9](https://github.com/mpecan/upsert/commit/c9283c9cfafdaa59f78a8562bce3d90a641d0d01))
* implement UpsertModel and related metadata provider for upsert operations ([14b22c0](https://github.com/mpecan/upsert/commit/14b22c07902e9e68580002cea7a64947c6a92ae9))
* initialize project structure with Spring Boot and Kotlin ([d8e0373](https://github.com/mpecan/upsert/commit/d8e0373273e98cbdb201a2be439a20ee60e28965))

## 0.0.1-SNAPSHOT (Unreleased)

Initial development version.

### Features
* Added MySQL and PostgreSQL upsert implementation documentation
* Enhanced UpsertModel to improve column handling and validation logic
* Added UpsertInfo data class and refactored UpsertMethodNameParser to use it
* Added upsertOnIdIgnoringAllFields method to allow insert-only operations
