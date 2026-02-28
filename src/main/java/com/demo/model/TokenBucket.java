package com.demo.model;

public class TokenBucket {

    private final long capacity;
    private long tokens;
    private final long refillRate; 
    private long lastRefillTime;

    public TokenBucket(long capacity, long refillRate) {
        this.capacity = capacity;
        this.tokens = capacity;
        this.refillRate = refillRate;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refill();

        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long timePassed = now - lastRefillTime;

        long tokensToAdd = (timePassed / 1000) * refillRate;

        if (tokensToAdd > 0) {
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }

    public long getTokens() {
        return tokens;
    }
}