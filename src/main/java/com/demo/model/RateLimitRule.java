package com.demo.model;

public class RateLimitRule {

    private String identifier;   
    private Tier tier;           
    private String endpoint;     
    private long capacity;
    private long refillRate;

    private int priority;

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
    public Tier getTier() { return tier; }
    public String getEndpoint() { return endpoint; }
    public long getCapacity() { return capacity; }
    public long getRefillRate() { return refillRate; }
    public int getPriority() { return priority; }
}