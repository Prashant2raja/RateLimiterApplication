package com.demo.service;

import com.demo.model.Tier;
import com.demo.model.TierAssignment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TierService {

    private final ConcurrentHashMap<String, TierAssignment> userTiers = new ConcurrentHashMap<>();
    private final AtomicLong version = new AtomicLong();
    private final Path storagePath;

    public TierService() {
        this(Paths.get("data", "tiers.properties"));
    }

    TierService(Path storagePath) {
        this.storagePath = storagePath;
        loadAssignments();
    }

    public void assignTier(String identifier, Tier tier) {
        assignTier(identifier, tier, null);
    }

    public void assignTier(String identifier, Tier tier, Long expiresAtEpochSeconds) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        if (tier == null) {
            throw new IllegalArgumentException("Tier must not be null");
        }

        userTiers.put(normalizedIdentifier, new TierAssignment(tier, expiresAtEpochSeconds));
        version.incrementAndGet();
        persistAssignments();
    }

    public Tier getTier(String identifier) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        TierAssignment assignment = userTiers.get(normalizedIdentifier);
        if (assignment == null) {
            return Tier.FREE;
        }

        Long expiresAtEpochSeconds = assignment.getExpiresAtEpochSeconds();
        if (expiresAtEpochSeconds != null && expiresAtEpochSeconds <= Instant.now().getEpochSecond()) {
            userTiers.remove(normalizedIdentifier);
            version.incrementAndGet();
            persistAssignments();
            return Tier.FREE;
        }

        return assignment.getTier();
    }

    public Long getTierExpiry(String identifier) {
        TierAssignment assignment = userTiers.get(normalizeIdentifier(identifier));
        return assignment == null ? null : assignment.getExpiresAtEpochSeconds();
    }

    public long getVersion() {
        return version.get();
    }

    public int getAssignedTierCount() {
        return userTiers.size();
    }

    private String normalizeIdentifier(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier must not be null");
        }

        String normalized = identifier.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Identifier must not be blank");
        }

        return normalized;
    }

    private void loadAssignments() {
        if (!Files.exists(storagePath)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(storagePath)) {
            properties.load(inputStream);
            for (String identifier : properties.stringPropertyNames()) {
                String value = properties.getProperty(identifier);
                String[] parts = value.split("\\|", -1);
                Tier tier = Tier.valueOf(parts[0]);
                Long expiresAtEpochSeconds = parts.length > 1 && !parts[1].isBlank()
                        ? Long.parseLong(parts[1])
                        : null;
                userTiers.put(identifier, new TierAssignment(tier, expiresAtEpochSeconds));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read tier assignments from " + storagePath, exception);
        }
    }

    private void persistAssignments() {
        try {
            Path parent = storagePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Properties properties = new Properties();
            userTiers.forEach((identifier, assignment) -> {
                String expiry = assignment.getExpiresAtEpochSeconds() == null
                        ? ""
                        : String.valueOf(assignment.getExpiresAtEpochSeconds());
                properties.setProperty(identifier, assignment.getTier().name() + "|" + expiry);
            });

            try (OutputStream outputStream = Files.newOutputStream(storagePath)) {
                properties.store(outputStream, "Persisted tier assignments");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store tier assignments to " + storagePath, exception);
        }
    }
}
