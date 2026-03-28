package com.demo.service;

import com.demo.model.TokenBucket;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBucketManager {

    private final Cache<String, TokenBucket> buckets = Caffeine.newBuilder()
            .maximumSize(200_000)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    public TokenBucket getBucket(String key, long capacity, double refillRate) {
        return buckets.asMap().computeIfAbsent(key, ignored -> new TokenBucket(capacity, refillRate));
    }

    public void reset(String key) {
        buckets.invalidate(key);
    }

    public void resetAllForUser(String identifier) {
        buckets.asMap().keySet().removeIf(key -> key.startsWith(identifier + "|"));
    }

    public long getBucketCount() {
        return buckets.estimatedSize();
    }
}
