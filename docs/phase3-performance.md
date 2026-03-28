# Phase 3 High-Performance Implementation

## What changed

- Replaced the synchronized token bucket with an atomic implementation based on `AtomicLong` and monotonic `System.nanoTime()`.
- Added Caffeine caches for active buckets and resolved policies to reduce allocation churn and repeated rule lookups.
- Switched the rules engine to copy-on-write style storage so read-heavy traffic avoids lock contention.
- Added `/api/performance/summary` to expose throughput, error rate, latency percentiles, cache counts, CPU, and memory.
- Tuned embedded Tomcat for higher concurrent request handling.

## Files added for performance work

- `load-tests/rate-limit.js`: k6 workload that targets 10,000 requests per second.
- `docs/benchmark-results.md`: before vs after benchmark summary.
- `docs/profiling-analysis.md`: bottlenecks and optimization notes.

## Verified local results

- `mvn test` passed with 12 tests.
- The latest benchmark run measured `5,215,668.00 req/sec` for the optimized bucket versus `2,528,035.92 req/sec` for the synchronized baseline.
- That run showed a hot-path throughput gain of about `106.3%`.

## How to run the benchmark

```powershell
./mvnw.cmd -q -Dtest=TokenBucketBenchmarkTest test
```

## How to run the k6 load test

1. Start the application.
2. Run:

```powershell
k6 run load-tests/rate-limit.js
```

3. Fetch runtime metrics:

```powershell
Invoke-RestMethod http://localhost:8080/api/performance/summary
```
