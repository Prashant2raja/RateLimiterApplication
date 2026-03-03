package com.demo.model;

public class TokenBucket {

    private final long capacity;
    private double tokens;
    private final double refillRate; 
    private long lastRefillTime;

    public TokenBucket(long capacity, double refillRate) {
        this.capacity = capacity;
        this.tokens = capacity;
        this.refillRate = refillRate;
        this.lastRefillTime = System.currentTimeMillis();
    }

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
            return; // no refill if rate is 0
        }

        long now = System.currentTimeMillis();
        double secondsPassed = (now - lastRefillTime) / 1000.0;

        if (secondsPassed <= 0) {
            return;
        }

        double tokensToAdd = secondsPassed * refillRate;

        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefillTime = now;
    }

    public synchronized long getRemainingTokens() {
        refill();
        return (long) tokens;
    }

    public long getCapacity() {
        return capacity;
    }
}