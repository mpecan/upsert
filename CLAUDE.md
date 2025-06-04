# Upsert Project Guidelines

## Build & Test Commands

- Build: `./gradlew build`
- Test all: `./gradlew test`
- Single test:
  `./gradlew test --tests "io.github.mpecan.upsert.integration.MySqlRepositoryIntegrationTest.should insert new jpa entity using repository"`
- Debug tests: `./gradlew test --debug`

## Performance Test Commands

Performance tests are excluded from regular test runs and must be run explicitly:

- **All performance tests**: `./gradlew performanceTest`
- **MySQL performance tests only**: `./gradlew performanceTestMySql`
- **PostgreSQL performance tests only**: `./gradlew performanceTestPostgreSql`

Performance tests compare upsert operations with Spring Data JPA `saveAll()` operations across different scenarios:
- Insert performance (new entities)
- Update performance (existing entities)
- Mixed insert/update performance
- Batch size impact analysis

**Note**: Performance tests take significantly longer than unit tests (several minutes) and require Docker for TestContainers.

## Code Style

- Kotlin with strict null safety (JSR-305 = strict)
- 4-space indentation, no tabs
- Maximum line length of 120 characters
- KDoc format for documentation comments
- Order imports alphabetically, group by package
- Descriptive test names with backtick notation (`should do something when condition`)
- Proper exception handling with logging
- Prefer immutable data when possible

## Naming Conventions

- Classes: PascalCase
- Functions/properties: camelCase
- Constants: SCREAMING_SNAKE_CASE
- Test classes should end with "Test"
- Interface methods should be well-documented with KDoc