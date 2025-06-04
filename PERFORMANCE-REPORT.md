# Performance Test Report
Generated on: 2025-06-04 11:22:23

## Summary

This report compares the performance of two approaches for inserting/updating entities:
1. `upsertAll` - Uses a custom implementation that generates SQL UPSERT statements
2. `saveAll` - Uses the standard Spring Data JPA implementation

Each test was run multiple times to get an average performance measurement, reducing the impact of external factors.

## Insert Performance

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 3,67 ms | 28,25 ms | -24,58 ms | 13,01% |
| MySQL | 10 | 6,37 ms | 26,51 ms | -20,14 ms | 24,04% |
| PostgreSQL | 100 | 8,29 ms | 97,81 ms | -89,52 ms | 8,48% |
| MySQL | 100 | 11,12 ms | 123,07 ms | -111,95 ms | 9,04% |
| PostgreSQL | 1000 | 29,21 ms | 468,41 ms | -439,19 ms | 6,24% |
| MySQL | 1000 | 54,09 ms | 795,74 ms | -741,65 ms | 6,80% |

## Mixed Insert-Update Performance

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 1,02 ms | 8,49 ms | -7,46 ms | 12,05% |
| MySQL | 10 | 2,64 ms | 14,31 ms | -11,67 ms | 18,43% |
| PostgreSQL | 100 | 2,95 ms | 50,63 ms | -47,67 ms | 5,83% |
| MySQL | 100 | 6,59 ms | 90,12 ms | -83,53 ms | 7,31% |
| PostgreSQL | 1000 | 23,58 ms | 415,39 ms | -391,81 ms | 5,68% |
| MySQL | 1000 | 39,71 ms | 741,19 ms | -701,48 ms | 5,36% |

## Update Performance

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 1,12 ms | 5,54 ms | -4,42 ms | 20,25% |
| MySQL | 10 | 3,46 ms | 14,18 ms | -10,72 ms | 24,41% |
| PostgreSQL | 100 | 3,57 ms | 47,06 ms | -43,48 ms | 7,59% |
| MySQL | 100 | 7,01 ms | 90,31 ms | -83,30 ms | 7,76% |
| PostgreSQL | 1000 | 32,60 ms | 432,76 ms | -400,17 ms | 7,53% |
| MySQL | 1000 | 77,15 ms | 770,61 ms | -693,46 ms | 10,01% |

## Batch Size Performance [total of 1000]

### PostgreSQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|----------------------|----------------------|-----------------|----------|
| 10 | 64,45 ms | 451,15 ms | -386,70 ms | 14,3% |
| 50 | 39,87 ms | 424,91 ms | -385,03 ms | 9,4% |
| 100 | 30,89 ms | 411,85 ms | -380,96 ms | 7,5% |
| 200 | 28,85 ms | 422,20 ms | -393,34 ms | 6,8% |
| 500 | 26,82 ms | 422,80 ms | -395,98 ms | 6,3% |
| 1000 | 24,10 ms | 421,47 ms | -397,37 ms | 5,7% |

### MySQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|----------------------|----------------------|-----------------|----------|
| 10 | 492,34 ms | 995,91 ms | -503,57 ms | 49,4% |
| 50 | 68,72 ms | 761,92 ms | -693,20 ms | 9,0% |
| 100 | 51,28 ms | 777,07 ms | -725,79 ms | 6,6% |
| 200 | 45,07 ms | 767,93 ms | -722,87 ms | 5,9% |
| 500 | 51,58 ms | 751,14 ms | -699,56 ms | 6,9% |
| 1000 | 44,56 ms | 769,31 ms | -724,75 ms | 5,8% |


