package com.demo.dto;

import com.demo.model.Tier;

public class TierAssignRequest {

    private String identifier;
    private Tier tier;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Tier getTier() {
        return tier;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }
}