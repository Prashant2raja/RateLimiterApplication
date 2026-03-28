package com.demo.model;

public record ResolvedRateLimitPolicy(
        String bucketKey,
        long capacity,
        double refillRate
) {
}
