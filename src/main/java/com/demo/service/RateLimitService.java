package com.demo.service;

import com.demo.model.*;
import com.demo.config.TierConfiguration;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final TokenBucketManager tokenBucketManager;
    private final TierService tierService;
    private final RuleEngineService ruleEngineService;

    public RateLimitService(TokenBucketManager tokenBucketManager,
                            TierService tierService,
                            RuleEngineService ruleEngineService) {
        this.tokenBucketManager = tokenBucketManager;
        this.tierService = tierService;
        this.ruleEngineService = ruleEngineService;
    }

    public RateLimitResult checkLimit(String identifier, String endpoint) {

        // 1️⃣ Get user tier
        Tier tier = tierService.getTier(identifier);

        // 2️⃣ Check rule override
        RateLimitRule rule =
                ruleEngineService.findApplicableRule(identifier, tier, endpoint);

        long capacity;
        double refillRate;   // ✅ Use double everywhere

        if (rule != null) {
            capacity = rule.getCapacity();
            refillRate = rule.getRefillRate();   // long → auto converts to double
        } else {
            TierConfig config = TierConfiguration.getConfig(tier);
            capacity = config.getCapacity();
            refillRate = config.getRefillRate();
        }

        // 3️⃣ Endpoint-based key
        String bucketKey = identifier + ":" + endpoint;

        TokenBucket bucket = tokenBucketManager.getBucket(
                bucketKey,
                capacity,
                refillRate
        );

        // 4️⃣ Try consume
        boolean allowed = bucket.tryConsume();

        // 5️⃣ Return professional result
        return new RateLimitResult(
                allowed,
                bucket.getRemainingTokens(),
                bucket.getCapacity(),
                bucket.getResetTimeSeconds()
        );
    }

    public void resetLimit(String identifier) {
        tokenBucketManager.resetAllForUser(identifier);
    }
}