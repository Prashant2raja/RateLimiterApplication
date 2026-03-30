package com.demo.model;

public class TierAssignment {

    private Tier tier;
    private Long expiresAtEpochSeconds;

    public TierAssignment() {
    }

    public TierAssignment(Tier tier, Long expiresAtEpochSeconds) {
        this.tier = tier;
        this.expiresAtEpochSeconds = expiresAtEpochSeconds;
    }

    public Tier getTier() {
        return tier;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public Long getExpiresAtEpochSeconds() {
        return expiresAtEpochSeconds;
    }

    public void setExpiresAtEpochSeconds(Long expiresAtEpochSeconds) {
        this.expiresAtEpochSeconds = expiresAtEpochSeconds;
    }
}
