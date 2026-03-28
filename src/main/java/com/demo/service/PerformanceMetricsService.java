package com.demo.service;

import com.demo.model.PerformanceSnapshot;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.LongAdder;

@Service
public class PerformanceMetricsService {

    private static final int LATENCY_SAMPLE_SIZE = 50_000;
    private static final double NANOS_TO_MILLIS = 1_000_000d;

    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder allowedRequests = new LongAdder();
    private final LongAdder deniedRequests = new LongAdder();
    private final LongAdder totalLatencyNanos = new LongAdder();
    private final AtomicLong maxLatencyNanos = new AtomicLong();
    private final AtomicLongArray recentLatencies = new AtomicLongArray(LATENCY_SAMPLE_SIZE);
    private final AtomicLong sampleCursor = new AtomicLong();
    private final long startedAtNanos = System.nanoTime();

    public void record(boolean allowed, long latencyNanos) {
        totalRequests.increment();
        totalLatencyNanos.add(latencyNanos);

        if (allowed) {
            allowedRequests.increment();
        } else {
            deniedRequests.increment();
        }

        long slot = sampleCursor.getAndIncrement();
        recentLatencies.set((int) (slot % LATENCY_SAMPLE_SIZE), latencyNanos);
        maxLatencyNanos.accumulateAndGet(latencyNanos, Math::max);
    }

    public PerformanceSnapshot snapshot(long activeBuckets, long cachedPolicies) {
        long total = totalRequests.sum();
        long allowed = allowedRequests.sum();
        long denied = deniedRequests.sum();
        double uptimeSeconds = Math.max(1d, (System.nanoTime() - startedAtNanos) / 1_000_000_000d);
        long[] latencies = copyLatencies();

        return new PerformanceSnapshot(
                total,
                allowed,
                denied,
                total / uptimeSeconds,
                total == 0 ? 0 : (double) denied / total,
                percentile(latencies, 0.50),
                percentile(latencies, 0.95),
                percentile(latencies, 0.99),
                percentile(latencies, 0.999),
                total == 0 ? 0 : (totalLatencyNanos.sum() / (double) total) / NANOS_TO_MILLIS,
                maxLatencyNanos.get() / NANOS_TO_MILLIS,
                activeBuckets,
                cachedPolicies,
                processCpuLoad(),
                usedMemoryMb()
        );
    }

    private long[] copyLatencies() {
        int size = (int) Math.min(sampleCursor.get(), LATENCY_SAMPLE_SIZE);
        long[] values = new long[size];

        for (int index = 0; index < size; index++) {
            values[index] = recentLatencies.get(index);
        }

        Arrays.sort(values);
        return values;
    }

    private double percentile(long[] values, double percentile) {
        if (values.length == 0) {
            return 0;
        }

        int index = (int) Math.min(values.length - 1, Math.ceil(percentile * values.length) - 1);
        return values[index] / NANOS_TO_MILLIS;
    }

    private double processCpuLoad() {
        var bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean osBean) {
            double load = osBean.getProcessCpuLoad();
            return load < 0 ? 0 : load * 100;
        }
        return 0;
    }

    private long usedMemoryMb() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }
}
