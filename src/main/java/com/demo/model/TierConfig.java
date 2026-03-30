package com.demo.model;

public class TierConfig {

    private long capacity;
    private double refillRate;

    public TierConfig() {
    }

    public TierConfig(long capacity, double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public double getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(double refillRate) {
        this.refillRate = refillRate;
    }
}
