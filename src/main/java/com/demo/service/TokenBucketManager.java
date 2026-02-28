package com.demo.service;

import java.util.concurrent.ConcurrentHashMap;

import com.demo.model.TokenBucket;

public class TokenBucketManager {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private final long capacity = 20;      
    private final long refillRate = 0;     

    public boolean allowRequest(String identifier) {

        // Create bucket if not exists
        buckets.putIfAbsent(identifier, new TokenBucket(capacity, refillRate));

        // Try consuming token
        return buckets.get(identifier).tryConsume();
    }

    public void reset(String identifier) {
        buckets.remove(identifier);
    }

    public TokenBucket getBucket(String identifier) {
        return buckets.get(identifier);
    }
}