package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.demo.dto.RateLimitRequest;
import com.demo.model.RateLimitResult;
import com.demo.service.RateLimitService;

@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitController {

    private final RateLimitService service;

    public RateLimitController(RateLimitService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<String> checkLimit(@RequestBody RateLimitRequest request) {

        RateLimitResult result = service.checkLimit(
                request.getIdentifier(),
                request.getEndpoint()
        );

        if (!result.isAllowed()) {
            return ResponseEntity.status(429)
                    .header("X-RateLimit-Limit", String.valueOf(result.getCapacity()))
                    .header("X-RateLimit-Remaining", "0")
                    .header("X-RateLimit-Reset", String.valueOf(result.getResetTime()))
                    .body("Too Many Requests");
        }

        return ResponseEntity.ok()
                .header("X-RateLimit-Limit", String.valueOf(result.getCapacity()))
                .header("X-RateLimit-Remaining", String.valueOf(result.getRemainingTokens()))
                .header("X-RateLimit-Reset", String.valueOf(result.getResetTime()))
                .body("Request Allowed");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> reset(@RequestBody RateLimitRequest request) {
        service.resetLimit(request.getIdentifier());
        return ResponseEntity.ok("Rate limit reset successful");
    }
}