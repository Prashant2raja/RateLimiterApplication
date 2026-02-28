package com.demo.service;

import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final TokenBucketManager manager = new TokenBucketManager();

    public boolean checkLimit(String identifier) {
        return manager.allowRequest(identifier);
    }

    public void resetLimit(String identifier) {
        manager.reset(identifier);
    }
}