package com.demo.service;


import com.demo.config.TierConfiguration;
import com.demo.model.RateLimitResult;
import com.demo.model.RateLimitRule;
import com.demo.model.Tier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    @TempDir
    Path tempDir;

    private TokenBucketManager manager;
    private TierService tierService;
    private RuleEngineService ruleEngine;
    private RateLimitService service;
    private TierConfiguration tierConfiguration;

    @BeforeEach
    void setup() {
        manager = new TokenBucketManager();
        tierService = new TierService(tempDir.resolve("tiers.properties"));
        ruleEngine = new RuleEngineService();
        tierConfiguration = new TierConfiguration();
        service = new RateLimitService(
                tierConfiguration,
                manager,
                tierService,
                ruleEngine,
                new PerformanceMetricsService()
        );
    }

    @Test
    void basicRequestAllowed() {
        RateLimitResult result = service.checkLimit("user1", "/api/search");
        assertTrue(result.isAllowed());
    }

    @Test
    void ruleBlocksAfterLimit() {
        ruleEngine.addRule(new RateLimitRule("user1", null, "/api/search", 2, 0, 1));

        service.checkLimit("user1", "/api/search");
        service.checkLimit("user1", "/api/search");

        RateLimitResult result = service.checkLimit("user1", "/api/search");
        assertFalse(result.isAllowed());
    }

    @Test
    void resetWorks() {
        ruleEngine.addRule(new RateLimitRule("user1", null, "/api/search", 1, 0, 1));
        service.checkLimit("user1", "/api/search");

        service.resetLimit("user1");

        RateLimitResult result = service.checkLimit("user1", "/api/search");
        assertTrue(result.isAllowed());
    }

    @Test
    void endpointIsolationWorks() {
        ruleEngine.addRule(new RateLimitRule("user1", null, "/api/search", 1, 0, 1));
        service.checkLimit("user1", "/api/search");

        RateLimitResult blocked = service.checkLimit("user1", "/api/search");
        RateLimitResult otherEndpoint = service.checkLimit("user1", "/api/other");

        assertFalse(blocked.isAllowed());
        assertTrue(otherEndpoint.isAllowed());
    }

    @Test
    void differentUsersStayIsolated() {
        ruleEngine.addRule(new RateLimitRule("user1", null, "/api/search", 1, 0, 1));
        service.checkLimit("user1", "/api/search");

        RateLimitResult user1Blocked = service.checkLimit("user1", "/api/search");
        RateLimitResult user2Allowed = service.checkLimit("user2", "/api/search");

        assertFalse(user1Blocked.isAllowed());
        assertTrue(user2Allowed.isAllowed());
    }

    @Test
    void tierFallbackWorks() {
        tierService.assignTier("user1", Tier.PRO);
        RateLimitResult result = service.checkLimit("user1", "/api/search");
        assertTrue(result.isAllowed());
    }

    @Test
    void remainingTokensDecrease() {
        ruleEngine.addRule(new RateLimitRule("user1", null, "/api/search", 5, 0, 1));

        RateLimitResult first = service.checkLimit("user1", "/api/search");
        RateLimitResult second = service.checkLimit("user1", "/api/search");

        assertTrue(second.getRemainingTokens() < first.getRemainingTokens());
    }

    @Test
    void capacityAppliedCorrectly() {
        ruleEngine.addRule(new RateLimitRule("user1", null, "/api/search", 4, 0, 1));

        RateLimitResult result = service.checkLimit("user1", "/api/search");
        assertEquals(4, result.getCapacity());
    }

    @Test
    void rulePriorityRespected() {
        ruleEngine.addRule(new RateLimitRule("user1", null, "/api/search", 10, 0, 2));
        ruleEngine.addRule(new RateLimitRule("user1", null, "/api/search", 2, 0, 1));

        service.checkLimit("user1", "/api/search");
        service.checkLimit("user1", "/api/search");

        RateLimitResult result = service.checkLimit("user1", "/api/search");
        assertFalse(result.isAllowed());
    }

    @Test
    void resetClearsAllEndpoints() {
        ruleEngine.addRule(new RateLimitRule("user1", null, "/api/search", 1, 0, 1));
        service.checkLimit("user1", "/api/search");

        service.resetLimit("user1");

        RateLimitResult result = service.checkLimit("user1", "/api/search");
        assertTrue(result.isAllowed());
    }

    @Test
    void cacheRefreshesAfterRuleChange() {
        RateLimitResult initial = service.checkLimit("user1", "/api/cache");
        assertEquals(100, initial.getCapacity());

        ruleEngine.addRule(new RateLimitRule("user1", null, "/api/cache", 3, 0, 1));

        RateLimitResult updated = service.checkLimit("user1", "/api/cache");
        assertEquals(3, updated.getCapacity());
    }

    @Test
    void expiredTierFallsBackToFree() {
        tierService.assignTier("user1", Tier.PRO, Instant.now().getEpochSecond() - 5);

        assertEquals(Tier.FREE, tierService.getTier("user1"));
    }

    @Test
    void tierAssignmentPersistsAcrossServiceInstances() {
        tierService.assignTier(" user1 ", Tier.ENTERPRISE, null);

        TierService reloaded = new TierService(tempDir.resolve("tiers.properties"));

        assertEquals(Tier.ENTERPRISE, reloaded.getTier("user1"));
    }
}
