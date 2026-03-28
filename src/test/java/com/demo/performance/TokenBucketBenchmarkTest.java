package com.demo.performance;

import com.demo.model.TokenBucket;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketBenchmarkTest {

    @Test
    void optimizedBucketOutperformsSynchronizedBaseline() throws Exception {
        BenchmarkResult baseline = runBenchmark(new SynchronizedTokenBucket(200_000, 0), 8, 25_000);
        BenchmarkResult optimized = runBenchmark(new BucketAdapter(new TokenBucket(200_000, 0)), 8, 25_000);

        System.out.println("Baseline throughput req/sec: " + baseline.throughput());
        System.out.println("Optimized throughput req/sec: " + optimized.throughput());
        System.out.println("Baseline avg latency ms: " + baseline.averageLatencyMillis());
        System.out.println("Optimized avg latency ms: " + optimized.averageLatencyMillis());

        assertTrue(optimized.throughput() > baseline.throughput());
    }

    private BenchmarkResult runBenchmark(ConsumableBucket bucket, int threads, int operationsPerThread) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger consumed = new AtomicInteger();
        List<Future<Long>> futures = new ArrayList<>();

        long benchmarkStarted = System.nanoTime();

        for (int threadIndex = 0; threadIndex < threads; threadIndex++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();

                long latencyTotal = 0;
                for (int op = 0; op < operationsPerThread; op++) {
                    long before = System.nanoTime();
                    if (bucket.tryConsume()) {
                        consumed.incrementAndGet();
                    }
                    latencyTotal += System.nanoTime() - before;
                }
                return latencyTotal;
            }));
        }

        ready.await(5, TimeUnit.SECONDS);
        benchmarkStarted = System.nanoTime();
        start.countDown();

        long totalLatency = 0;
        for (Future<Long> future : futures) {
            totalLatency += future.get(30, TimeUnit.SECONDS);
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long elapsedNanos = System.nanoTime() - benchmarkStarted;
        int totalRequests = threads * operationsPerThread;
        double throughput = totalRequests / (elapsedNanos / 1_000_000_000d);
        double averageLatencyMillis = (totalLatency / (double) totalRequests) / 1_000_000d;

        return new BenchmarkResult(throughput, averageLatencyMillis, consumed.get());
    }

    private record BenchmarkResult(double throughput, double averageLatencyMillis, int consumed) {
    }

    private interface ConsumableBucket {
        boolean tryConsume();
    }

    private record BucketAdapter(TokenBucket tokenBucket) implements ConsumableBucket {
        @Override
        public boolean tryConsume() {
            return tokenBucket.tryConsume();
        }
    }

    private static class SynchronizedTokenBucket implements ConsumableBucket {

        private final long capacity;
        private double tokens;
        private final double refillRate;
        private long lastRefillTime;

        private SynchronizedTokenBucket(long capacity, double refillRate) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.refillRate = refillRate;
            this.lastRefillTime = System.currentTimeMillis();
        }

        @Override
        public synchronized boolean tryConsume() {
            refill();

            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        private void refill() {
            if (refillRate <= 0) {
                return;
            }

            long now = System.currentTimeMillis();
            double secondsPassed = (now - lastRefillTime) / 1000.0;

            if (secondsPassed > 0) {
                double tokensToAdd = secondsPassed * refillRate;
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
}
