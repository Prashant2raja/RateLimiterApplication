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

    public boolean checkLimit(String identifier, String endpoint) {

        Tier tier = tierService.getTier(identifier);

        RateLimitRule rule =
                ruleEngineService.findApplicableRule(identifier, tier, endpoint);

        long capacity;
        double refillRate;

        if (rule != null) {
            capacity = rule.getCapacity();
            refillRate = rule.getRefillRate();
        } else {
            TierConfig config = TierConfiguration.getConfig(tier);
            capacity = config.getCapacity();
            refillRate = config.getRefillRate();
        }

        // 3️⃣ Important: use identifier + endpoint as key
        String bucketKey = identifier + ":" + endpoint;

        TokenBucket bucket = tokenBucketManager.getBucket(
                bucketKey,
                capacity,
                refillRate
        );

        return bucket.tryConsume();
    }

    public void resetLimit(String identifier) {
        tokenBucketManager.reset(identifier);
    }
}