package com.demo.model;

public class RateLimitRule {

    private String identifier;
    private Tier tier;
    private String endpoint;
    private long capacity;
    private long refillRate;
    private int priority;

    public RateLimitRule() {
    }

    public RateLimitRule(String identifier, Tier tier, String endpoint,
                         long capacity, long refillRate, int priority) {
        this.identifier = identifier;
        this.tier = tier;
        this.endpoint = endpoint;
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.priority = priority;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public Tier getTier() { return tier; }
    public void setTier(Tier tier) { this.tier = tier; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public long getCapacity() { return capacity; }
    public void setCapacity(long capacity) { this.capacity = capacity; }
    public long getRefillRate() { return refillRate; }
    public void setRefillRate(long refillRate) { this.refillRate = refillRate; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
