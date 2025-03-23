# Performance Testing Framework

This document describes the performance testing framework used to compare the performance of
`upsertAll` vs `saveAll` operations in the upsert library.

## Overview

The performance testing framework is designed to:

1. Run each test multiple times (default: 10 repetitions) to get an average performance measurement
2. Generate a markdown report showing the differences between the two approaches
3. Support testing with different database types (MySQL, PostgreSQL)

## Test Types

The framework includes the following test types:

1. **Insert Performance**: Tests the performance of inserting new entities
2. **Update Performance**: Tests the performance of updating existing entities
3. **Mixed Insert-Update Performance**: Tests the performance of a mix of inserting and updating
   entities
4. **Batch Size Performance**: Tests the performance impact of different batch sizes

## Running the Tests

The performance tests are implemented as JUnit tests with the `@Tag("performance")` annotation. You
can run them using the standard Gradle test task:

```bash
./gradlew test -PincludeTags="performance"
```

To run only MySQL tests:

```bash
./gradlew test --tests "io.github.mpecan.upsert.performance.MySqlPerformanceTest"
```

To run only PostgreSQL tests:

```bash
./gradlew test --tests "io.github.mpecan.upsert.performance.PostgreSqlPerformanceTest"
```

## Performance Report

The performance tests now use a JUnit TestExecutionListener that automatically generates a report
after test execution. When you run tests with the `@Tag("performance")` annotation, a markdown
report is automatically generated at `PERFORMANCE-REPORT.md` in the project root directory.

This report includes:

1. Tables showing the average performance of `upsertAll` vs `saveAll` for each test type
2. Comparisons across different entity counts and batch sizes
3. Performance ratios to easily see the relative performance difference
4. Grouping by database type for batch size performance tests

## Implementation Details

The performance testing framework consists of the following components:

### PerformanceTestUtils

A utility class that provides methods to:

- Run a test multiple times and calculate average performance
- Store test results for later reporting
- Provide access to all collected test results

### PerformanceTestListener

A JUnit TestExecutionListener that:

- Automatically runs after all tests are completed
- Collects all performance test results
- Triggers the report generation process

### PerformanceReportGenerator

A utility class that generates a markdown report from the test results, including:

- Tables for each test type
- Grouping by database type
- Formatting of performance metrics
- Calculation of performance differences and ratios

### Test Classes

- `MySqlPerformanceTest`: Performance tests for MySQL
- `PostgreSqlPerformanceTest`: Performance tests for PostgreSQL

Each test class:

- Uses TestContainers to spin up a database container for testing
- Is annotated with `@Tag("performance")` for targeted test execution

## Customizing the Tests

The number of repetitions for each test can be adjusted by modifying the `repetitions` parameter in
the `runPerformanceTest` method calls in the test classes.

The entity counts and batch sizes can be adjusted by modifying the `@ValueSource` annotations and
batch size lists in the test methods.
