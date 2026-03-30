package com.demo.controller;

import com.demo.model.*;
import com.demo.service.RuleEngineService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rule")
public class RuleController {

    private final RuleEngineService ruleEngineService;

    public RuleController(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }

    @PostMapping("/add")
    public String addRule(@RequestBody RateLimitRule rule) {
        ruleEngineService.addRule(rule);
        return "Rule added successfully";
    }

    @GetMapping
    public java.util.List<RateLimitRule> listRules() {
        return ruleEngineService.getRules();
    }
}
