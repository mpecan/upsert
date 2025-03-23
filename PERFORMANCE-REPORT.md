# Performance Test Report

Generated on: 2025-03-23 17:10:07

## Summary

This report compares the performance of two approaches for inserting/updating entities:
1. `upsertAll` - Uses a custom implementation that generates SQL UPSERT statements
2. `saveAll` - Uses the standard Spring Data JPA implementation

Each test was run multiple times to get an average performance measurement, reducing the impact of
external factors.

## Insert Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 6,58 ms         | 25,66 ms         | -19,08 ms  | 25,64%    |
| PostgreSQL | 10           | 3,12 ms         | 10,39 ms         | -7,27 ms   | 30,03%    |
| MySQL      | 100          | 6,22 ms         | 109,18 ms        | -102,96 ms | 5,70%     |
| PostgreSQL | 100          | 3,38 ms         | 41,19 ms         | -37,81 ms  | 8,21%     |
| MySQL      | 1000         | 28,51 ms        | 621,63 ms        | -593,12 ms | 4,59%     |
| PostgreSQL | 1000         | 20,41 ms        | 361,30 ms        | -340,89 ms | 5,65%     |

## Mixed Insert-Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 1,67 ms         | 10,63 ms         | -8,96 ms   | 15,74%    |
| PostgreSQL | 10           | 763,56 µs       | 4,22 ms          | -3,45 ms   | 18,11%    |
| MySQL      | 100          | 5,11 ms         | 67,15 ms         | -62,05 ms  | 7,61%     |
| PostgreSQL | 100          | 2,23 ms         | 36,63 ms         | -34,40 ms  | 6,09%     |
| MySQL      | 1000         | 25,00 ms        | 560,27 ms        | -535,28 ms | 4,46%     |
| PostgreSQL | 1000         | 18,51 ms        | 355,74 ms        | -337,23 ms | 5,20%     |

## Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 1,48 ms         | 7,52 ms          | -6,04 ms   | 19,70%    |
| PostgreSQL | 10           | 632,57 µs       | 4,34 ms          | -3,71 ms   | 14,58%    |
| MySQL      | 100          | 5,21 ms         | 126,79 ms        | -121,58 ms | 4,11%     |
| PostgreSQL | 100          | 2,26 ms         | 36,18 ms         | -33,92 ms  | 6,26%     |
| MySQL      | 1000         | 40,93 ms        | 722,54 ms        | -681,62 ms | 5,66%     |
| PostgreSQL | 1000         | 18,89 ms        | 346,65 ms        | -327,76 ms | 5,45%     |

## Batch Size Performance [total of 1000]

### MySQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 155,66 ms       | 884,34 ms        | -728,68 ms | 17,6%     |
| 50         | 45,11 ms        | 504,02 ms        | -458,90 ms | 9,0%      |
| 100        | 33,96 ms        | 505,87 ms        | -471,91 ms | 6,7%      |
| 200        | 27,27 ms        | 497,84 ms        | -470,57 ms | 5,5%      |
| 500        | 22,91 ms        | 500,20 ms        | -477,28 ms | 4,6%      |
| 1000       | 22,38 ms        | 498,39 ms        | -476,01 ms | 4,5%      |

### PostgreSQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 42,18 ms        | 388,77 ms        | -346,59 ms | 10,8%     |
| 50         | 24,38 ms        | 353,48 ms        | -329,11 ms | 6,9%      |
| 100        | 19,20 ms        | 350,74 ms        | -331,54 ms | 5,5%      |
| 200        | 18,16 ms        | 348,73 ms        | -330,56 ms | 5,2%      |
| 500        | 17,89 ms        | 347,01 ms        | -329,12 ms | 5,2%      |
| 1000       | 17,53 ms        | 345,62 ms        | -328,09 ms | 5,1%      |


