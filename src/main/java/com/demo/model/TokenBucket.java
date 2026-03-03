package com.demo.model;

public class TokenBucket {

    private final long capacity;
    private double tokens;
    private final double refillRate; // tokens per second
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
            return; // No refill if refillRate is 0
        }

        long now = System.currentTimeMillis();
        double secondsPassed = (now - lastRefillTime) / 1000.0;

        if (secondsPassed > 0) {
            double tokensToAdd = secondsPassed * refillRate;
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }

    public synchronized long getRemainingTokens() {
        refill();
        return (long) tokens;
    }

    public long getCapacity() {
        return capacity;
    }

    public synchronized long getResetTimeSeconds() {

        if (tokens >= 1 || refillRate <= 0) {
            return System.currentTimeMillis() / 1000;
        }

        double tokensNeeded = 1 - tokens;
        long secondsUntilNextToken = (long) Math.ceil(tokensNeeded / refillRate);

        return (System.currentTimeMillis() / 1000) + secondsUntilNextToken;
    }
}