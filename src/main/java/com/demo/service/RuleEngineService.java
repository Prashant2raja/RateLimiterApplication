package com.demo.service;

import com.demo.model.RateLimitRule;
import com.demo.model.Tier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RuleEngineService {

    private final AtomicReference<List<RateLimitRule>> rules = new AtomicReference<>(List.of());
    private final AtomicLong version = new AtomicLong();

    public void addRule(RateLimitRule rule) {
        while (true) {
            List<RateLimitRule> current = rules.get();
            List<RateLimitRule> updated = new ArrayList<>(current);
            updated.add(rule);
            updated.sort(Comparator.comparingInt(RateLimitRule::getPriority));

            if (rules.compareAndSet(current, List.copyOf(updated))) {
                version.incrementAndGet();
                return;
            }
        }
    }

    public RateLimitRule findApplicableRule(String identifier, Tier tier, String endpoint) {
        for (RateLimitRule rule : rules.get()) {
            boolean matchIdentifier = rule.getIdentifier() == null || rule.getIdentifier().equals(identifier);
            boolean matchTier = rule.getTier() == null || rule.getTier().equals(tier);
            boolean matchEndpoint = rule.getEndpoint() == null || rule.getEndpoint().equals(endpoint);

            if (matchIdentifier && matchTier && matchEndpoint) {
                return rule;
            }
        }

        return null;
    }

    public long getVersion() {
        return version.get();
    }

    public List<RateLimitRule> getRules() {
        return rules.get();
    }

    public int getRuleCount() {
        return rules.get().size();
    }
}
