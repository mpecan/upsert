# Upsert Project Guidelines

## Build & Test Commands

- Build: `./gradlew build`
- Test all: `./gradlew test`
- Single test:
  `./gradlew test --tests "si.pecan.upsert.integration.MySqlRepositoryIntegrationTest.should insert new jpa entity using repository"`
- Debug tests: `./gradlew test --debug`

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