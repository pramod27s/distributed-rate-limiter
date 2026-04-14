package dev.bnacar.distributedratelimiter.models;

public class ApiKeyInfoResponse {
    private final String key;
    private final String displayName;
    private final String description;
    private final String userType;
    private final boolean active;
    private final int capacity;
    private final int refillRate;
    private final String algorithm;
    private final long lastUsedAt;
    private final ApiKeyUsageStatsResponse usage;

    public ApiKeyInfoResponse(String key,
                              String displayName,
                              String description,
                              String userType,
                              boolean active,
                              int capacity,
                              int refillRate,
                              String algorithm,
                              long lastUsedAt,
                              ApiKeyUsageStatsResponse usage) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.userType = userType;
        this.active = active;
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.algorithm = algorithm;
        this.lastUsedAt = lastUsedAt;
        this.usage = usage;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getUserType() {
        return userType;
    }

    public boolean isActive() {
        return active;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getRefillRate() {
        return refillRate;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public long getLastUsedAt() {
        return lastUsedAt;
    }

    public ApiKeyUsageStatsResponse getUsage() {
        return usage;
    }
}

