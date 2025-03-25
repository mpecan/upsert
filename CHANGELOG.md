# Changelog

## [1.2.0](https://github.com/mpecan/upsert/compare/v1.1.0...v1.2.0) (2025-03-25)


### Features

* type mappers and json ([#36](https://github.com/mpecan/upsert/issues/36)) ([80bcdea](https://github.com/mpecan/upsert/commit/80bcdea8d6e9589312cca6c927af9a4ff121f00e))


### Bug Fixes

* **deps:** update dependency org.xerial:sqlite-jdbc to v3.49.1.0 ([#34](https://github.com/mpecan/upsert/issues/34)) ([359efe6](https://github.com/mpecan/upsert/commit/359efe63482efd494392b43cec4acf0baa3932fb))


### Documentation

* update version in README.md to 1.1.0 ([#32](https://github.com/mpecan/upsert/issues/32)) ([ea9e4f6](https://github.com/mpecan/upsert/commit/ea9e4f642ca02c252475448ec6929f2f36ccab73))

## [1.1.0](https://github.com/mpecan/upsert/compare/v1.0.1...v1.1.0) (2025-03-23)


### Features

* add auto-configuration for Upsert functionality and update performance report formatting ([#30](https://github.com/mpecan/upsert/issues/30)) ([f703b6c](https://github.com/mpecan/upsert/commit/f703b6c1bb331a1de8ed3f4aeaf38916718626f2))
* add performance tests for upsert operations in MySQL and PostgreSQL ([#27](https://github.com/mpecan/upsert/issues/27)) ([bbb0593](https://github.com/mpecan/upsert/commit/bbb0593c3d8060391801c1941ec35098f8d5bb69))
* implement performance testing framework for upsert operations in MySQL and PostgreSQL ([#29](https://github.com/mpecan/upsert/issues/29)) ([e55ef63](https://github.com/mpecan/upsert/commit/e55ef6388d5b48cfc99ae6e6403998ee095894e5))

## [1.0.1](https://github.com/mpecan/upsert/compare/v1.0.0...v1.0.1) (2025-03-21)


### Bug Fixes

* fix release process ([#21](https://github.com/mpecan/upsert/issues/21)) ([92ee1d3](https://github.com/mpecan/upsert/commit/92ee1d333807fb7c18398001de704f71dd0b7d86))

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
