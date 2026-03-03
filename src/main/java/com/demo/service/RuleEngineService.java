package com.demo.service;

import com.demo.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RuleEngineService {

    private final List<RateLimitRule> rules = new ArrayList<>();

    public void addRule(RateLimitRule rule) {
        rules.add(rule);
        rules.sort(Comparator.comparingInt(RateLimitRule::getPriority));
    }

    public RateLimitRule findApplicableRule(String identifier,
                                            Tier tier,
                                            String endpoint) {

        for (RateLimitRule rule : rules) {

            boolean matchIdentifier =
                    rule.getIdentifier() == null ||
                    rule.getIdentifier().equals(identifier);

            boolean matchTier =
                    rule.getTier() == null ||
                    rule.getTier().equals(tier);

            boolean matchEndpoint =
                    rule.getEndpoint() == null ||
                    rule.getEndpoint().equals(endpoint);

            if (matchIdentifier && matchTier && matchEndpoint) {
                return rule;
            }
        }

        return null;
    }
}