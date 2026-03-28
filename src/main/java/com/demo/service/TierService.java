package com.demo.service;

import com.demo.model.Tier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TierService {

    private final ConcurrentHashMap<String, Tier> userTiers = new ConcurrentHashMap<>();
    private final AtomicLong version = new AtomicLong();

    public void assignTier(String identifier, Tier tier) {
        userTiers.put(identifier, tier);
        version.incrementAndGet();
    }

    public Tier getTier(String identifier) {
        return userTiers.getOrDefault(identifier, Tier.FREE);
    }

    public long getVersion() {
        return version.get();
    }
}
