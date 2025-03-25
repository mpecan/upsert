# Performance Test Report

Generated on: 2025-03-25 18:45:33

## Summary

This report compares the performance of two approaches for inserting/updating entities:
1. `upsertAll` - Uses a custom implementation that generates SQL UPSERT statements
2. `saveAll` - Uses the standard Spring Data JPA implementation

Each test was run multiple times to get an average performance measurement, reducing the impact of
external factors.

## Insert Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| PostgreSQL | 10           | 8,54 ms         | 19,74 ms         | -11,19 ms  | 43,29%    |
| PostgreSQL | 100          | 25,34 ms        | 58,33 ms         | -32,99 ms  | 43,45%    |
| PostgreSQL | 1000         | 215,70 ms       | 425,77 ms        | -210,07 ms | 50,66%    |

## Mixed Insert-Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| PostgreSQL | 10           | 2,82 ms         | 7,43 ms          | -4,61 ms   | 37,96%    |
| PostgreSQL | 100          | 24,10 ms        | 52,59 ms         | -28,50 ms  | 45,82%    |
| PostgreSQL | 1000         | 210,34 ms       | 395,22 ms        | -184,88 ms | 53,22%    |

## Update Performance

| Database   | Entity Count | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|--------------|-----------------|------------------|------------|-----------|
| PostgreSQL | 10           | 3,07 ms         | 4,76 ms          | -1,68 ms   | 64,62%    |
| PostgreSQL | 100          | 21,92 ms        | 40,08 ms         | -18,16 ms  | 54,68%    |
| PostgreSQL | 1000         | 201,96 ms       | 375,35 ms        | -173,39 ms | 53,81%    |

## Batch Size Performance [total of 1000]

### PostgreSQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|-----------------|------------------|------------|-----------|
| 10         | 233,90 ms       | 405,93 ms        | -172,02 ms | 57,6%     |
| 50         | 220,46 ms       | 382,81 ms        | -162,35 ms | 57,6%     |
| 100        | 198,58 ms       | 383,52 ms        | -184,95 ms | 51,8%     |
| 200        | 209,42 ms       | 390,90 ms        | -181,47 ms | 53,6%     |
| 500        | 200,45 ms       | 389,69 ms        | -189,24 ms | 51,4%     |
| 1000       | 195,76 ms       | 375,14 ms        | -179,38 ms | 52,2%     |


