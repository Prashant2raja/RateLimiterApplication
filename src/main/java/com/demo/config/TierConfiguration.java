package com.demo.config;

import com.demo.model.Tier;
import com.demo.model.TierConfig;

import java.util.HashMap;
import java.util.Map;

public class TierConfiguration {

    private static final Map<Tier, TierConfig> tierConfigs = new HashMap<>();

    static {
        tierConfigs.put(Tier.FREE, new TierConfig(100, 100.0 / 3600)); 
        tierConfigs.put(Tier.PRO, new TierConfig(1000, 1000.0 / 3600));
        tierConfigs.put(Tier.ENTERPRISE, new TierConfig(5000, 5000.0 / 3600));
        tierConfigs.put(Tier.UNLIMITED, new TierConfig(Long.MAX_VALUE, Long.MAX_VALUE));
    }

    public static TierConfig getConfig(Tier tier) {
        return tierConfigs.get(tier);
    }
}