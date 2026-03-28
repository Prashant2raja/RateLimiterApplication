package com.demo.service;

import com.demo.config.TierConfiguration;
import com.demo.model.RateLimitResult;
import com.demo.model.RateLimitRule;
import com.demo.model.ResolvedRateLimitPolicy;
import com.demo.model.Tier;
import com.demo.model.TierConfig;
import com.demo.model.TokenBucket;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final TokenBucketManager tokenBucketManager;
    private final TierService tierService;
    private final RuleEngineService ruleEngineService;
    private final PerformanceMetricsService performanceMetricsService;
    private final Cache<PolicyCacheKey, ResolvedRateLimitPolicy> policyCache = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    public RateLimitService(TokenBucketManager tokenBucketManager,
                            TierService tierService,
                            RuleEngineService ruleEngineService,
                            PerformanceMetricsService performanceMetricsService) {
        this.tokenBucketManager = tokenBucketManager;
        this.tierService = tierService;
        this.ruleEngineService = ruleEngineService;
        this.performanceMetricsService = performanceMetricsService;
    }

    public RateLimitResult checkLimit(String identifier, String endpoint) {
        long startedAt = System.nanoTime();
        boolean allowed = false;

        try {
            PolicyCacheKey cacheKey = new PolicyCacheKey(
                    identifier,
                    endpoint,
                    tierService.getVersion(),
                    ruleEngineService.getVersion()
            );

            ResolvedRateLimitPolicy policy = policyCache.get(cacheKey, ignored -> resolvePolicy(identifier, endpoint));
            TokenBucket bucket = tokenBucketManager.getBucket(
                    policy.bucketKey(),
                    policy.capacity(),
                    policy.refillRate()
            );

            allowed = bucket.tryConsume();

            return new RateLimitResult(
                    allowed,
                    bucket.getRemainingTokens(),
                    bucket.getCapacity(),
                    bucket.getResetTimeSeconds()
            );
        } finally {
            performanceMetricsService.record(allowed, System.nanoTime() - startedAt);
        }
    }

    public void resetLimit(String identifier) {
        tokenBucketManager.resetAllForUser(identifier);
    }

    public long getCachedPolicyCount() {
        return policyCache.estimatedSize();
    }

    private ResolvedRateLimitPolicy resolvePolicy(String identifier, String endpoint) {
        Tier tier = tierService.getTier(identifier);
        RateLimitRule rule = ruleEngineService.findApplicableRule(identifier, tier, endpoint);

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

        String bucketKey = identifier
                + "|"
                + endpoint
                + "|"
                + capacity
                + "|"
                + Double.doubleToLongBits(refillRate);

        return new ResolvedRateLimitPolicy(bucketKey, capacity, refillRate);
    }

    public record PolicyCacheKey(
            String identifier,
            String endpoint,
            long tierVersion,
            long ruleVersion
    ) {
    }
}
