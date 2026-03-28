package com.demo.controller;

import com.demo.model.PerformanceSnapshot;
import com.demo.service.PerformanceMetricsService;
import com.demo.service.RateLimitService;
import com.demo.service.TokenBucketManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceMetricsService performanceMetricsService;
    private final TokenBucketManager tokenBucketManager;
    private final RateLimitService rateLimitService;

    public PerformanceController(PerformanceMetricsService performanceMetricsService,
                                 TokenBucketManager tokenBucketManager,
                                 RateLimitService rateLimitService) {
        this.performanceMetricsService = performanceMetricsService;
        this.tokenBucketManager = tokenBucketManager;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/summary")
    public PerformanceSnapshot summary() {
        return performanceMetricsService.snapshot(
                tokenBucketManager.getBucketCount(),
                rateLimitService.getCachedPolicyCount()
        );
    }
}
