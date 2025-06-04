# Performance Test Report
Generated on: 2025-06-04 09:39:49

## Summary

This report compares the performance of two approaches for inserting/updating entities:
1. `upsertAll` - Uses a custom implementation that generates SQL UPSERT statements
2. `saveAll` - Uses the standard Spring Data JPA implementation

Each test was run multiple times to get an average performance measurement, reducing the impact of external factors.

## Insert Performance

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 2,66 ms | 20,20 ms | -17,54 ms | 13,18% |
| MySQL | 10 | 5,35 ms | 23,21 ms | -17,86 ms | 23,05% |
| PostgreSQL | 100 | 5,15 ms | 68,28 ms | -63,14 ms | 7,54% |
| MySQL | 100 | 8,02 ms | 113,07 ms | -105,05 ms | 7,09% |
| PostgreSQL | 1000 | 25,95 ms | 469,33 ms | -443,37 ms | 5,53% |
| MySQL | 1000 | 43,12 ms | 730,02 ms | -686,91 ms | 5,91% |

## Mixed Insert-Update Performance

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 965,40 µs | 9,27 ms | -8,30 ms | 10,41% |
| MySQL | 10 | 2,15 ms | 10,28 ms | -8,13 ms | 20,95% |
| PostgreSQL | 100 | 3,29 ms | 51,55 ms | -48,26 ms | 6,37% |
| MySQL | 100 | 6,16 ms | 84,05 ms | -77,89 ms | 7,33% |
| PostgreSQL | 1000 | 22,74 ms | 402,48 ms | -379,74 ms | 5,65% |
| MySQL | 1000 | 45,74 ms | 722,93 ms | -677,19 ms | 6,33% |

## Update Performance

| Database | Entity Count | Avg Upsert Time  | Avg SaveAll Time  | Difference  | Ratio (%) |
|----------|--------------|----------------------|----------------------|-----------------|----------|
| PostgreSQL | 10 | 795,87 µs | 5,23 ms | -4,44 ms | 15,21% |
| MySQL | 10 | 2,32 ms | 11,78 ms | -9,47 ms | 19,68% |
| PostgreSQL | 100 | 2,85 ms | 42,28 ms | -39,43 ms | 6,74% |
| MySQL | 100 | 6,34 ms | 80,40 ms | -74,06 ms | 7,88% |
| PostgreSQL | 1000 | 25,02 ms | 397,97 ms | -372,95 ms | 6,29% |
| MySQL | 1000 | 134,70 ms | 814,28 ms | -679,58 ms | 16,54% |

## Batch Size Performance [total of 1000]

### PostgreSQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|----------------------|----------------------|-----------------|----------|
| 10 | 60,11 ms | 426,02 ms | -365,90 ms | 14,1% |
| 50 | 31,43 ms | 414,61 ms | -383,18 ms | 7,6% |
| 100 | 23,40 ms | 393,28 ms | -369,88 ms | 5,9% |
| 200 | 20,77 ms | 402,74 ms | -381,97 ms | 5,2% |
| 500 | 25,05 ms | 421,89 ms | -396,83 ms | 5,9% |
| 1000 | 23,15 ms | 412,72 ms | -389,57 ms | 5,6% |

### MySQL

| Batch Size | Avg Upsert Time | Avg SaveAll Time | Difference | Ratio (%) |
|------------|----------------------|----------------------|-----------------|----------|
| 10 | 242,39 ms | 1,00 s | -758,87 ms | 24,2% |
| 50 | 66,62 ms | 751,73 ms | -685,11 ms | 8,9% |
| 100 | 50,16 ms | 750,67 ms | -700,50 ms | 6,7% |
| 200 | 71,13 ms | 777,77 ms | -706,64 ms | 9,1% |
| 500 | 35,52 ms | 709,73 ms | -674,21 ms | 5,0% |
| 1000 | 36,49 ms | 724,00 ms | -687,51 ms | 5,0% |


