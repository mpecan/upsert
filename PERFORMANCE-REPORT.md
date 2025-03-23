# Performance Test Report

Generated on: 2025-03-23 19:24:35

## Summary

This report compares the performance of two approaches for inserting/updating entities:
1. `upsertAll` - Uses a custom implementation that generates SQL UPSERT statements
2. `saveAll` - Uses the standard Spring Data JPA implementation

Each test was run multiple times to get an average performance measurement, reducing the impact of
external factors.

## Insert Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 7,10 ms         | 28,20 ms         | -21,10 ms  | 25,19%    |
| PostgreSQL | 10           | 3,50 ms         | 10,80 ms         | -7,30 ms   | 32,40%    |
| MySQL      | 100          | 6,66 ms         | 144,54 ms        | -137,87 ms | 4,61%     |
| PostgreSQL | 100          | 3,72 ms         | 54,55 ms         | -50,83 ms  | 6,81%     |
| MySQL      | 1000         | 29,66 ms        | 759,79 ms        | -730,13 ms | 3,90%     |
| PostgreSQL | 1000         | 20,22 ms        | 372,38 ms        | -352,17 ms | 5,43%     |

## Mixed Insert-Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 1,58 ms         | 11,10 ms         | -9,52 ms   | 14,23%    |
| PostgreSQL | 10           | 1,90 ms         | 6,21 ms          | -4,31 ms   | 30,64%    |
| MySQL      | 100          | 4,73 ms         | 68,50 ms         | -63,77 ms  | 6,91%     |
| PostgreSQL | 100          | 2,38 ms         | 40,07 ms         | -37,69 ms  | 5,94%     |
| MySQL      | 1000         | 24,89 ms        | 538,15 ms        | -513,26 ms | 4,62%     |
| PostgreSQL | 1000         | 19,74 ms        | 389,29 ms        | -369,55 ms | 5,07%     |

## Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| MySQL      | 10           | 1,73 ms         | 8,38 ms          | -6,65 ms   | 20,62%    |
| PostgreSQL | 10           | 673,08 µs       | 4,33 ms          | -3,66 ms   | 15,53%    |
| MySQL      | 100          | 4,77 ms         | 59,46 ms         | -54,69 ms  | 8,02%     |
| PostgreSQL | 100          | 2,35 ms         | 37,13 ms         | -34,77 ms  | 6,34%     |
| MySQL      | 1000         | 37,31 ms        | 690,14 ms        | -652,83 ms | 5,41%     |
| PostgreSQL | 1000         | 20,91 ms        | 476,17 ms        | -455,25 ms | 4,39%     |

## Batch Size Performance [total of 1000]

### MySQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 163,12 ms       | 827,24 ms        | -664,11 ms | 19,7%     |
| 50         | 47,51 ms        | 515,11 ms        | -467,60 ms | 9,2%      |
| 100        | 35,21 ms        | 628,32 ms        | -593,11 ms | 5,6%      |
| 200        | 30,15 ms        | 668,74 ms        | -638,58 ms | 4,5%      |
| 500        | 23,67 ms        | 512,72 ms        | -489,05 ms | 4,6%      |
| 1000       | 22,96 ms        | 524,79 ms        | -501,83 ms | 4,4%      |

### PostgreSQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 52,93 ms        | 454,89 ms        | -401,96 ms | 11,6%     |
| 50         | 27,18 ms        | 457,81 ms        | -430,62 ms | 5,9%      |
| 100        | 20,74 ms        | 382,04 ms        | -361,30 ms | 5,4%      |
| 200        | 19,32 ms        | 450,32 ms        | -430,99 ms | 4,3%      |
| 500        | 19,75 ms        | 414,91 ms        | -395,16 ms | 4,8%      |
| 1000       | 18,65 ms        | 395,25 ms        | -376,60 ms | 4,7%      |


