package com.demo.service;

import com.demo.model.RateLimitResult;
import com.demo.model.RateLimitRule;
import com.demo.model.Tier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimitServiceTest {

    private TokenBucketManager manager;
    private TierService tierService;
    private RuleEngineService ruleEngine;
    private RateLimitService service;

    @BeforeEach
    void setup() {
        manager = new TokenBucketManager();
        tierService = new TierService();
        ruleEngine = new RuleEngineService();
        service = new RateLimitService(manager, tierService, ruleEngine);
    }

    // 1️⃣ Basic request allowed
    @Test
    void testBasicRequestAllowed() {
        RateLimitResult result =
                service.checkLimit("user1", "/api/search");

        assertTrue(result.isAllowed());
    }

    // 2️⃣ Rule blocks after capacity
    @Test
    void testRuleBlocksAfterLimit() {

        ruleEngine.addRule(
                new RateLimitRule("user1", null,
                        "/api/search", 2, 0, 1)
        );

        service.checkLimit("user1", "/api/search");
        service.checkLimit("user1", "/api/search");

        RateLimitResult result =
                service.checkLimit("user1", "/api/search");

        assertFalse(result.isAllowed());
    }

    // 3️⃣ Reset works
    @Test
    void testResetWorks() {

        ruleEngine.addRule(
                new RateLimitRule("user1", null,
                        "/api/search", 1, 0, 1)
        );

        service.checkLimit("user1", "/api/search");

        service.resetLimit("user1");

        RateLimitResult result =
                service.checkLimit("user1", "/api/search");

        assertTrue(result.isAllowed());
    }

    // 4️⃣ Endpoint isolation
    @Test
    void testEndpointIsolation() {

        ruleEngine.addRule(
                new RateLimitRule("user1", null,
                        "/api/search", 1, 0, 1)
        );

        service.checkLimit("user1", "/api/search");

        RateLimitResult blocked =
                service.checkLimit("user1", "/api/search");

        RateLimitResult otherEndpoint =
                service.checkLimit("user1", "/api/other");

        assertFalse(blocked.isAllowed());
        assertTrue(otherEndpoint.isAllowed());
    }

    // 5️⃣ Different users isolated
    @Test
    void testDifferentUsersIsolation() {

        ruleEngine.addRule(
                new RateLimitRule("user1", null,
                        "/api/search", 1, 0, 1)
        );

        service.checkLimit("user1", "/api/search");

        RateLimitResult user1Blocked =
                service.checkLimit("user1", "/api/search");

        RateLimitResult user2Allowed =
                service.checkLimit("user2", "/api/search");

        assertFalse(user1Blocked.isAllowed());
        assertTrue(user2Allowed.isAllowed());
    }

    // 6️⃣ Tier fallback works
    @Test
    void testTierFallback() {

        tierService.assignTier("user1", Tier.PRO);

        RateLimitResult result =
                service.checkLimit("user1", "/api/search");

        assertTrue(result.isAllowed());
    }

    // 7️⃣ Remaining tokens decrease
    @Test
    void testRemainingTokensDecrease() {

        ruleEngine.addRule(
                new RateLimitRule("user1", null,
                        "/api/search", 5, 0, 1)
        );

        RateLimitResult first =
                service.checkLimit("user1", "/api/search");

        RateLimitResult second =
                service.checkLimit("user1", "/api/search");

        assertTrue(second.getRemainingTokens()
                < first.getRemainingTokens());
    }

    // 8️⃣ Capacity applied correctly
    @Test
    void testCapacityApplied() {

        ruleEngine.addRule(
                new RateLimitRule("user1", null,
                        "/api/search", 4, 0, 1)
        );

        RateLimitResult result =
                service.checkLimit("user1", "/api/search");

        assertEquals(4, result.getCapacity());
    }

    // 9️⃣ Rule priority respected
    @Test
    void testRulePriority() {

        ruleEngine.addRule(
                new RateLimitRule("user1", null,
                        "/api/search", 10, 0, 2)
        );

        ruleEngine.addRule(
                new RateLimitRule("user1", null,
                        "/api/search", 2, 0, 1)
        );

        service.checkLimit("user1", "/api/search");
        service.checkLimit("user1", "/api/search");

        RateLimitResult result =
                service.checkLimit("user1", "/api/search");

        assertFalse(result.isAllowed());
    }

    // 🔟 Reset clears all endpoints
    @Test
    void testResetClearsAllEndpoints() {

        ruleEngine.addRule(
                new RateLimitRule("user1", null,
                        "/api/search", 1, 0, 1)
        );

        service.checkLimit("user1", "/api/search");

        service.resetLimit("user1");

        RateLimitResult result =
                service.checkLimit("user1", "/api/search");

        assertTrue(result.isAllowed());
    }
}