package com.demo.service;

import com.demo.model.Tier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TierService {

    private final ConcurrentHashMap<String, Tier> userTiers = new ConcurrentHashMap<>();

    public void assignTier(String identifier, Tier tier) {
        userTiers.put(identifier, tier);
    }

    public Tier getTier(String identifier) {
        return userTiers.getOrDefault(identifier, Tier.FREE);
    }

    
}