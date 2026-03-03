package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.demo.dto.RateLimitRequest;
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

    boolean allowed = service.checkLimit(
            request.getIdentifier(),
            request.getEndpoint()
    );

    if (!allowed) {
        return ResponseEntity.status(429).body("Too Many Requests");
    }

    return ResponseEntity.ok("Request Allowed");
}

    @PostMapping("/reset")
    public ResponseEntity<String> reset(@RequestBody RateLimitRequest request) {
        service.resetLimit(request.getIdentifier());
        return ResponseEntity.ok("Rate limit reset successful");
    }
}