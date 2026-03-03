package com.demo.model;

public class RateLimitResult {

    private boolean allowed;
    private long remainingTokens;
    private long capacity;
    private long resetTime; 

    public RateLimitResult(boolean allowed, long remainingTokens, long capacity, long resetTime) {
        this.allowed = allowed;
        this.remainingTokens = remainingTokens;
        this.capacity = capacity;
        this.resetTime = resetTime;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getRemainingTokens() {
        return remainingTokens;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getResetTime() {
        return resetTime;
    }
}