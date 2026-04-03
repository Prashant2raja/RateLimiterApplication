package com.demo.model;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucket {

    private static final long TOKEN_SCALE = 1_000_000L;
    private static final long ONE_TOKEN = TOKEN_SCALE;

    private final long capacity;
    private final double refillRate;
    private final long capacityScaled;
    private final double refillScaledPerNano;
    private final AtomicLong availableTokens;
    private final AtomicLong lastRefillNanos;
    private final boolean unlimited;

    public TokenBucket(long capacity, double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.unlimited = capacity == Long.MAX_VALUE || refillRate == Long.MAX_VALUE;
        this.capacityScaled = unlimited ? Long.MAX_VALUE : safeMultiply(capacity, TOKEN_SCALE);
        this.refillScaledPerNano = unlimited
                ? 0
                : (refillRate * TOKEN_SCALE) / TimeUnit.SECONDS.toNanos(1);
        this.availableTokens = new AtomicLong(capacityScaled);
        this.lastRefillNanos = new AtomicLong(System.nanoTime());
    } 

    public boolean tryConsume() {
        if (unlimited) {
            return true;
        }

        refill();

        while (true) {
            long current = availableTokens.get();
            if (current < ONE_TOKEN) {
                return false;
            }
            if (availableTokens.compareAndSet(current, current - ONE_TOKEN)) {
                return true;
            }
        }
    }

    public long getRemainingTokens() {
        if (unlimited) {
            return Long.MAX_VALUE;
        }

        refill();
        return availableTokens.get() / TOKEN_SCALE;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getResetTimeSeconds() {
        long nowSeconds = System.currentTimeMillis() / 1000;

        if (unlimited) {
            return nowSeconds;
        }

        refill();
        long currentTokens = availableTokens.get();

        if (currentTokens >= ONE_TOKEN || refillRate <= 0) {
            return nowSeconds;
        }

        long tokensNeeded = ONE_TOKEN - currentTokens;
        long nanosUntilNextToken = (long) Math.ceil(tokensNeeded / refillScaledPerNano);
        long secondsUntilNextToken = Math.max(1L, TimeUnit.NANOSECONDS.toSeconds(nanosUntilNextToken));
        return nowSeconds + secondsUntilNextToken;
    }

    private void refill() {
        if (refillRate <= 0 || unlimited) {
            return;
        }

        long now = System.nanoTime();

        while (true) {
            long previous = lastRefillNanos.get();
            long elapsed = now - previous;

            if (elapsed <= 0) {
                return;
            }

            long refillAmount = (long) (elapsed * refillScaledPerNano);
            if (refillAmount <= 0) {
                return;
            }

            if (lastRefillNanos.compareAndSet(previous, now)) {
                availableTokens.getAndUpdate(current -> Math.min(capacityScaled, current + refillAmount));
                return;
            }
        }
    }

    private long safeMultiply(long left, long right) {
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }
}
