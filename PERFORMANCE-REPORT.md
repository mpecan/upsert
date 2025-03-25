# Performance Test Report

Generated on: 2025-03-25 20:43:16

## Summary

This report compares the performance of two approaches for inserting/updating entities:
1. `upsertAll` - Uses a custom implementation that generates SQL UPSERT statements
2. `saveAll` - Uses the standard Spring Data JPA implementation

Each test was run multiple times to get an average performance measurement, reducing the impact of
external factors.

## Insert Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 6,79 ms         | 28,35 ms         | -21,56 ms  | 23,94%    |
| PostgreSQL | 10           | 3,65 ms         | 10,17 ms         | -6,52 ms   | 35,88%    |
| MySQL      | 100          | 7,97 ms         | 110,59 ms        | -102,62 ms | 7,21%     |
| PostgreSQL | 100          | 3,31 ms         | 44,31 ms         | -41,00 ms  | 7,46%     |
| MySQL      | 1000         | 34,25 ms        | 683,43 ms        | -649,18 ms | 5,01%     |
| PostgreSQL | 1000         | 19,80 ms        | 358,75 ms        | -338,95 ms | 5,52%     |

## Mixed Insert-Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 2,92 ms         | 12,14 ms         | -9,22 ms   | 24,05%    |
| PostgreSQL | 10           | 683,56 µs       | 4,25 ms          | -3,57 ms   | 16,07%    |
| MySQL      | 100          | 4,61 ms         | 78,92 ms         | -74,31 ms  | 5,85%     |
| PostgreSQL | 100          | 2,32 ms         | 32,55 ms         | -30,23 ms  | 7,12%     |
| MySQL      | 1000         | 34,75 ms        | 640,06 ms        | -605,31 ms | 5,43%     |
| PostgreSQL | 1000         | 18,90 ms        | 400,51 ms        | -381,61 ms | 4,72%     |

## Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 2,13 ms         | 9,58 ms          | -7,45 ms   | 22,26%    |
| PostgreSQL | 10           | 650,23 µs       | 3,87 ms          | -3,22 ms   | 16,78%    |
| MySQL      | 100          | 7,95 ms         | 66,64 ms         | -58,68 ms  | 11,94%    |
| PostgreSQL | 100          | 2,37 ms         | 38,23 ms         | -35,87 ms  | 6,19%     |
| MySQL      | 1000         | 40,80 ms        | 756,69 ms        | -715,89 ms | 5,39%     |
| PostgreSQL | 1000         | 18,86 ms        | 360,94 ms        | -342,08 ms | 5,23%     |

## Batch Size Performance [total of 1000]

### MySQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 187,77 ms       | 922,61 ms        | -734,84 ms | 20,4%     |
| 50         | 58,12 ms        | 564,86 ms        | -506,74 ms | 10,3%     |
| 100        | 63,27 ms        | 747,97 ms        | -684,70 ms | 8,5%      |
| 200        | 51,27 ms        | 843,21 ms        | -791,94 ms | 6,1%      |
| 500        | 38,45 ms        | 756,86 ms        | -718,41 ms | 5,1%      |
| 1000       | 41,41 ms        | 932,54 ms        | -891,13 ms | 4,4%      |

### PostgreSQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 52,05 ms        | 406,03 ms        | -353,98 ms | 12,8%     |
| 50         | 23,17 ms        | 363,47 ms        | -340,30 ms | 6,4%      |
| 100        | 19,53 ms        | 355,55 ms        | -336,02 ms | 5,5%      |
| 200        | 17,95 ms        | 361,35 ms        | -343,40 ms | 5,0%      |
| 500        | 17,70 ms        | 363,01 ms        | -345,30 ms | 4,9%      |
| 1000       | 17,54 ms        | 358,58 ms        | -341,05 ms | 4,9%      |


