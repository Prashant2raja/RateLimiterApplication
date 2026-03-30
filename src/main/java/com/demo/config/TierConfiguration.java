package com.demo.config;

import com.demo.model.Tier;
import com.demo.model.TierConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "rate-limit")
public class TierConfiguration {

    private final Map<Tier, TierConfig> tiers = new EnumMap<>(Tier.class);

    public TierConfiguration() {
        tiers.put(Tier.FREE, new TierConfig(100, 100.0 / 3600));
        tiers.put(Tier.PRO, new TierConfig(1000, 1000.0 / 3600));
        tiers.put(Tier.ENTERPRISE, new TierConfig(5000, 5000.0 / 3600));
        tiers.put(Tier.UNLIMITED, new TierConfig(Long.MAX_VALUE, Long.MAX_VALUE));
    }

    public Map<Tier, TierConfig> getTiers() {
        return tiers;
    }

    public TierConfig getConfig(Tier tier) {
        return tiers.getOrDefault(tier, tiers.get(Tier.FREE));
    }
}
