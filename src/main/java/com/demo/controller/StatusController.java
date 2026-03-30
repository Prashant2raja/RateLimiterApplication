package com.demo.controller;

import com.demo.config.TierConfiguration;
import com.demo.model.StatusSnapshot;
import com.demo.service.RateLimitService;
import com.demo.service.RuleEngineService;
import com.demo.service.TierService;
import com.demo.service.TokenBucketManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final String applicationName;
    private final TierService tierService;
    private final RuleEngineService ruleEngineService;
    private final TokenBucketManager tokenBucketManager;
    private final RateLimitService rateLimitService;
    private final TierConfiguration tierConfiguration;

    public StatusController(@Value("${spring.application.name}") String applicationName,
                            TierService tierService,
                            RuleEngineService ruleEngineService,
                            TokenBucketManager tokenBucketManager,
                            RateLimitService rateLimitService,
                            TierConfiguration tierConfiguration) {
        this.applicationName = applicationName;
        this.tierService = tierService;
        this.ruleEngineService = ruleEngineService;
        this.tokenBucketManager = tokenBucketManager;
        this.rateLimitService = rateLimitService;
        this.tierConfiguration = tierConfiguration;
    }

    @GetMapping
    public StatusSnapshot status() {
        return new StatusSnapshot(
                applicationName,
                "UP",
                tierService.getAssignedTierCount(),
                ruleEngineService.getRuleCount(),
                tokenBucketManager.getBucketCount(),
                rateLimitService.getCachedPolicyCount(),
                tierConfiguration.getTiers()
        );
    }
}
