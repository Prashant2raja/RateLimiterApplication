# Benchmark Results

This document captures the local benchmark comparison between the previous synchronized token bucket design and the Phase 3 optimized implementation.

Benchmark run date: 2026-03-24
Benchmark harness: `TokenBucketBenchmarkTest`
Workload: 8 threads x 25,000 operations each

## Before vs after

| Metric | Baseline synchronized bucket | Optimized atomic bucket |
|---|---:|---:|
| Throughput (req/sec) | 2,528,035.92 | 5,215,668.00 |
| Average latency (ms) | 0.00228742 | 0.00106981 |
| Relative throughput | 1.00x | 2.06x |

## Visual summary

```text
Throughput
Baseline  : 2.53M req/sec  ################
Optimized : 5.22M req/sec  #################################

Average latency
Baseline  : 0.00228742 ms
Optimized : 0.00106981 ms
```

## Interpretation

- The optimized bucket improved raw throughput by about 106.3 percent in the latest local concurrency benchmark.
- Average latency was also lower in the latest run, though microbenchmarks can vary with JVM warmup and scheduling.
- The code path is now positioned for the larger end-to-end target, and `load-tests/rate-limit.js` is ready for HTTP-level validation at 10,000 requests per second.
