package com.demo.service;

import com.demo.model.TokenBucket;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBucketManager {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public TokenBucket getBucket(String key, long capacity, double refillRate) {

        return buckets.computeIfAbsent(key,
                k -> new TokenBucket(capacity, refillRate));
    }

    public void reset(String key) {
        buckets.remove(key);
    }

    public void resetAllForUser(String identifier) {
        buckets.keySet().removeIf(key -> key.startsWith(identifier + ":"));
    }
}