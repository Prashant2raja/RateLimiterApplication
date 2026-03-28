package com.demo.model;

public record PerformanceSnapshot(
        long totalRequests,
        long allowedRequests,
        long deniedRequests,
        double throughputPerSecond,
        double errorRate,
        double p50Millis,
        double p95Millis,
        double p99Millis,
        double p999Millis,
        double averageLatencyMillis,
        double maxLatencyMillis,
        long activeBuckets,
        long cachedPolicies,
        double processCpuLoad,
        long usedMemoryMb
) {
}
