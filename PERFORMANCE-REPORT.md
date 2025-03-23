# Performance Test Report

Generated on: 2025-03-23 16:23:37

## Summary

This report compares the performance of two approaches for inserting/updating entities:

1. `upsertAll` - Uses a custom implementation that generates SQL UPSERT statements
2. `saveAll` - Uses the standard Spring Data JPA implementation

Each test was run multiple times to get an average performance measurement, reducing the impact of
external factors.

## Insert Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 6,84 ms         | 25,42 ms         | -18,58 ms  | 26,90%    |
| PostgreSQL | 10           | 5,00 ms         | 16,37 ms         | -11,38 ms  | 30,51%    |
| MySQL      | 100          | 7,04 ms         | 110,25 ms        | -103,21 ms | 6,38%     |
| PostgreSQL | 100          | 3,64 ms         | 48,19 ms         | -44,55 ms  | 7,55%     |
| MySQL      | 1000         | 30,73 ms        | 620,86 ms        | -590,14 ms | 4,95%     |
| PostgreSQL | 1000         | 20,65 ms        | 421,71 ms        | -401,05 ms | 4,90%     |

## Mixed Insert-Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 1,52 ms         | 9,85 ms          | -8,32 ms   | 15,46%    |
| PostgreSQL | 10           | 650,38 µs       | 4,52 ms          | -3,87 ms   | 14,38%    |
| MySQL      | 100          | 4,36 ms         | 66,88 ms         | -62,52 ms  | 6,52%     |
| PostgreSQL | 100          | 2,83 ms         | 46,03 ms         | -43,20 ms  | 6,15%     |
| MySQL      | 1000         | 26,01 ms        | 659,09 ms        | -633,07 ms | 3,95%     |
| PostgreSQL | 1000         | 19,19 ms        | 415,05 ms        | -395,87 ms | 4,62%     |

## Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 1,63 ms         | 8,24 ms          | -6,61 ms   | 19,83%    |
| PostgreSQL | 10           | 704,15 µs       | 4,75 ms          | -4,05 ms   | 14,82%    |
| MySQL      | 100          | 6,05 ms         | 112,88 ms        | -106,83 ms | 5,36%     |
| PostgreSQL | 100          | 2,65 ms         | 41,84 ms         | -39,19 ms  | 6,33%     |
| MySQL      | 1000         | 36,65 ms        | 673,71 ms        | -637,06 ms | 5,44%     |
| PostgreSQL | 1000         | 18,86 ms        | 376,45 ms        | -357,59 ms | 5,01%     |

## Batch Size Performance [total of 1000]

### MySQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 151,52 ms       | 790,71 ms        | -639,19 ms | 19,2%     |
| 50         | 47,70 ms        | 573,61 ms        | -525,91 ms | 8,3%      |
| 100        | 35,69 ms        | 565,60 ms        | -529,91 ms | 6,3%      |
| 200        | 28,13 ms        | 553,97 ms        | -525,85 ms | 5,1%      |
| 500        | 25,43 ms        | 672,82 ms        | -647,39 ms | 3,8%      |
| 1000       | 24,02 ms        | 602,15 ms        | -578,14 ms | 4,0%      |

### PostgreSQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 43,12 ms        | 371,74 ms        | -328,63 ms | 11,6%     |
| 50         | 24,36 ms        | 352,67 ms        | -328,31 ms | 6,9%      |
| 100        | 21,28 ms        | 386,35 ms        | -365,08 ms | 5,5%      |
| 200        | 18,78 ms        | 368,87 ms        | -350,10 ms | 5,1%      |
| 500        | 18,60 ms        | 381,77 ms        | -363,17 ms | 4,9%      |
| 1000       | 18,38 ms        | 387,61 ms        | -369,23 ms | 4,7%      |


