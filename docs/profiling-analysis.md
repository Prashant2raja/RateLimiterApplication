# Profiling Analysis

## Primary bottlenecks observed in the original design

- `synchronized` on every `tryConsume`, `getRemainingTokens`, and `getResetTimeSeconds` serialized hot-path traffic.
- `System.currentTimeMillis()` introduced wall-clock sensitivity and coarser refill timing.
- `ConcurrentHashMap` bucket retention had no eviction policy, which can grow memory usage under high-cardinality traffic.
- Rule evaluation and tier lookup ran on every request without memoization.

## Optimization rationale

- Atomic compare-and-set reduces lock convoy effects under concurrency.
- `System.nanoTime()` gives monotonic timing and more stable refill calculations.
- Caffeine reduces repeated policy resolution cost and automatically evicts inactive entries.
- Copy-on-write rule snapshots favor the expected production pattern of frequent reads and infrequent writes.

## What to inspect during runtime

- `/api/performance/summary` for application-side latency percentiles and throughput.
- k6 output for end-to-end HTTP percentiles and failure rate.
- JVM CPU and heap usage while running the steady 10k requests/sec scenario.
