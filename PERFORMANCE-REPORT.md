# Performance Test Report
Generated on: 2025-06-24 11:21:49

## Summary

This report compares the performance of two approaches for inserting/updating entities:
1. `upsertAll` - Uses a custom implementation that generates SQL UPSERT statements
2. `saveAll` - Uses the standard Spring Data JPA implementation

Each test was run multiple times to get an average performance measurement, reducing the impact of external factors.

## Conditional Upsert (Timestamp)

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 5,39 ms | 27,50 ms | -22,11 ms | 19,59% |
| MySQL | 10 | 6,40 ms | 25,41 ms | -19,01 ms | 25,17% |
| PostgreSQL | 100 | 4,77 ms | 84,70 ms | -79,93 ms | 5,63% |
| MySQL | 100 | 14,10 ms | 133,97 ms | -119,87 ms | 10,53% |
| PostgreSQL | 1000 | 34,41 ms | 537,89 ms | -503,48 ms | 6,40% |
| MySQL | 1000 | 65,26 ms | 939,41 ms | -874,14 ms | 6,95% |

## Insert Performance

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 2,04 ms | 7,00 ms | -4,96 ms | 29,15% |
| MySQL | 10 | 5,54 ms | 16,13 ms | -10,60 ms | 34,31% |
| PostgreSQL | 100 | 4,99 ms | 73,90 ms | -68,91 ms | 6,75% |
| MySQL | 100 | 9,13 ms | 124,33 ms | -115,19 ms | 7,35% |
| PostgreSQL | 1000 | 29,88 ms | 730,46 ms | -700,58 ms | 4,09% |
| MySQL | 1000 | 44,69 ms | 1,35 s | -1,30 s | 3,32% |

## Conditional Upsert (Mixed Insert/Update)

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 1,27 ms | 11,72 ms | -10,45 ms | 10,80% |
| MySQL | 10 | 4,35 ms | 16,80 ms | -12,45 ms | 25,89% |
| PostgreSQL | 100 | 4,03 ms | 89,09 ms | -85,07 ms | 4,52% |
| MySQL | 100 | 8,67 ms | 117,01 ms | -108,34 ms | 7,41% |
| PostgreSQL | 1000 | 39,19 ms | 643,00 ms | -603,81 ms | 6,09% |
| MySQL | 1000 | 62,12 ms | 1,24 s | -1,18 s | 5,01% |

## Mixed Insert-Update Performance

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 1,44 ms | 14,36 ms | -12,93 ms | 10,01% |
| MySQL | 10 | 6,01 ms | 31,05 ms | -25,04 ms | 19,36% |
| PostgreSQL | 100 | 3,48 ms | 79,46 ms | -75,98 ms | 4,38% |
| MySQL | 100 | 11,47 ms | 125,00 ms | -113,53 ms | 9,18% |
| PostgreSQL | 1000 | 30,86 ms | 788,81 ms | -757,94 ms | 3,91% |
| MySQL | 1000 | 49,00 ms | 1,48 s | -1,43 s | 3,32% |

## Update Performance

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 1,11 ms | 7,92 ms | -6,81 ms | 13,99% |
| MySQL | 10 | 4,57 ms | 22,21 ms | -17,64 ms | 20,58% |
| PostgreSQL | 100 | 4,66 ms | 95,46 ms | -90,80 ms | 4,88% |
| MySQL | 100 | 10,89 ms | 160,95 ms | -150,05 ms | 6,77% |
| PostgreSQL | 1000 | 46,20 ms | 944,04 ms | -897,84 ms | 4,89% |
| MySQL | 1000 | 75,68 ms | 1,49 s | -1,42 s | 5,07% |

## Batch Size Performance [total of 1000]

### PostgreSQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|----------------------|----------------------|-----------------|----------|
| 10 | 118,42 ms | 884,88 ms | -766,46 ms | 13,4% |
| 50 | 51,52 ms | 742,90 ms | -691,38 ms | 6,9% |
| 100 | 46,66 ms | 758,25 ms | -711,60 ms | 6,2% |
| 200 | 40,91 ms | 877,02 ms | -836,11 ms | 4,7% |
| 500 | 32,58 ms | 765,67 ms | -733,09 ms | 4,3% |
| 1000 | 28,52 ms | 699,08 ms | -670,56 ms | 4,1% |

### MySQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|----------------------|----------------------|-----------------|----------|
| 10 | 303,61 ms | 1,67 s | -1,37 s | 18,2% |
| 50 | 113,28 ms | 1,47 s | -1,36 s | 7,7% |
| 100 | 71,62 ms | 1,36 s | -1,29 s | 5,3% |
| 200 | 61,51 ms | 1,37 s | -1,31 s | 4,5% |
| 500 | 58,90 ms | 1,34 s | -1,28 s | 4,4% |
| 1000 | 61,63 ms | 1,28 s | -1,22 s | 4,8% |


## Conditional Upsert (Version)

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 1,67 ms | 9,36 ms | -7,69 ms | 17,84% |
| MySQL | 10 | 4,27 ms | 19,91 ms | -15,64 ms | 21,43% |
| PostgreSQL | 100 | 3,40 ms | 68,02 ms | -64,62 ms | 5,00% |
| MySQL | 100 | 13,50 ms | 93,31 ms | -79,81 ms | 14,47% |
| PostgreSQL | 1000 | 34,77 ms | 510,33 ms | -475,56 ms | 6,81% |
| MySQL | 1000 | 77,05 ms | 1,01 s | -928,84 ms | 7,66% |

## Conditional Upsert (High Contention)

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 1,04 ms | 3,50 ms | -2,46 ms | 29,66% |
| MySQL | 10 | 2,88 ms | 8,23 ms | -5,35 ms | 35,00% |
| PostgreSQL | 100 | 2,53 ms | 12,49 ms | -9,97 ms | 20,23% |
| MySQL | 100 | 7,48 ms | 23,86 ms | -16,38 ms | 31,35% |
| PostgreSQL | 1000 | 25,55 ms | 83,84 ms | -58,30 ms | 30,47% |
| MySQL | 1000 | 48,66 ms | 168,84 ms | -120,18 ms | 28,82% |

