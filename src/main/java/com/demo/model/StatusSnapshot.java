package com.demo.model;

import java.util.Map;

public record StatusSnapshot(
        String application,
        String status,
        int assignedTiers,
        int activeRules,
        long activeBuckets,
        long cachedPolicies,
        Map<Tier, TierConfig> tierConfigurations
) {
}
