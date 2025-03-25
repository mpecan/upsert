# Performance Test Report

Generated on: 2025-03-25 19:21:02

## Summary

This report compares the performance of two approaches for inserting/updating entities:
1. `upsertAll` - Uses a custom implementation that generates SQL UPSERT statements
2. `saveAll` - Uses the standard Spring Data JPA implementation

Each test was run multiple times to get an average performance measurement, reducing the impact of
external factors.

## Insert Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 7,52 ms         | 29,87 ms         | -22,35 ms  | 25,16%    |
| PostgreSQL | 10           | 11,74 ms        | 24,19 ms         | -12,45 ms  | 48,54%    |
| MySQL      | 100          | 8,43 ms         | 122,87 ms        | -114,44 ms | 6,86%     |
| PostgreSQL | 100          | 24,57 ms        | 44,67 ms         | -20,10 ms  | 55,00%    |
| MySQL      | 1000         | 37,06 ms        | 949,86 ms        | -912,79 ms | 3,90%     |
| PostgreSQL | 1000         | 203,70 ms       | 366,70 ms        | -162,99 ms | 55,55%    |

## Mixed Insert-Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 2,41 ms         | 16,10 ms         | -13,69 ms  | 14,97%    |
| PostgreSQL | 10           | 2,55 ms         | 4,82 ms          | -2,27 ms   | 52,95%    |
| MySQL      | 100          | 5,62 ms         | 82,92 ms         | -77,31 ms  | 6,77%     |
| PostgreSQL | 100          | 20,98 ms        | 38,84 ms         | -17,86 ms  | 54,02%    |
| MySQL      | 1000         | 29,14 ms        | 739,27 ms        | -710,12 ms | 3,94%     |
| PostgreSQL | 1000         | 211,91 ms       | 408,16 ms        | -196,25 ms | 51,92%    |

## Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 1,96 ms         | 9,91 ms          | -7,95 ms   | 19,80%    |
| PostgreSQL | 10           | 2,44 ms         | 4,07 ms          | -1,64 ms   | 59,82%    |
| MySQL      | 100          | 4,90 ms         | 67,64 ms         | -62,73 ms  | 7,25%     |
| PostgreSQL | 100          | 19,26 ms        | 36,96 ms         | -17,70 ms  | 52,10%    |
| MySQL      | 1000         | 41,98 ms        | 815,36 ms        | -773,37 ms | 5,15%     |
| PostgreSQL | 1000         | 221,04 ms       | 393,85 ms        | -172,81 ms | 56,12%    |

## Batch Size Performance [total of 1000]

### MySQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 206,93 ms       | 1,12 s           | -913,15 ms | 18,5%     |
| 50         | 50,77 ms        | 541,63 ms        | -490,87 ms | 9,4%      |
| 100        | 42,80 ms        | 692,73 ms        | -649,93 ms | 6,2%      |
| 200        | 38,12 ms        | 736,74 ms        | -698,62 ms | 5,2%      |
| 500        | 26,93 ms        | 606,34 ms        | -579,41 ms | 4,4%      |
| 1000       | 25,70 ms        | 592,87 ms        | -567,16 ms | 4,3%      |

### PostgreSQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 223,65 ms       | 395,71 ms        | -172,05 ms | 56,5%     |
| 50         | 203,18 ms       | 337,52 ms        | -134,35 ms | 60,2%     |
| 100        | 209,79 ms       | 379,03 ms        | -169,24 ms | 55,3%     |
| 200        | 218,16 ms       | 403,45 ms        | -185,29 ms | 54,1%     |
| 500        | 204,54 ms       | 370,99 ms        | -166,45 ms | 55,1%     |
| 1000       | 204,44 ms       | 366,18 ms        | -161,74 ms | 55,8%     |


