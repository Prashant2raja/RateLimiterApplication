package com.demo.model;

public class TierConfig {

    private long capacity;
    private double refillRate; 

    public TierConfig(long capacity, double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
    }

    public long getCapacity() {
        return capacity;
    }

    public double getRefillRate() {
        return refillRate;
    }
}