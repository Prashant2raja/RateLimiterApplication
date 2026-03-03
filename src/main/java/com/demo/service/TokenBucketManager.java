package com.demo.service;

import com.demo.model.TokenBucket;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBucketManager {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    
    public TokenBucket getBucket(String identifier, long capacity, double refillRate) {

        return buckets.computeIfAbsent(identifier,
                key -> new TokenBucket(capacity, refillRate));
    }

    public void reset(String identifier) {
        buckets.remove(identifier);
    }
}