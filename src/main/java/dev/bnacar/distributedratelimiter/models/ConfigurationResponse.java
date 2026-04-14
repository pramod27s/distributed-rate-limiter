package dev.bnacar.distributedratelimiter.models;

import dev.bnacar.distributedratelimiter.ratelimit.RateLimiterConfiguration;
import dev.bnacar.distributedratelimiter.ratelimit.RateLimitAlgorithm;

import java.util.Map;
import java.util.HashMap;

public class ConfigurationResponse {
    private final int capacity;
    private final int refillRate;
    private final long cleanupIntervalMs;
    private final RateLimitAlgorithm algorithm;
    private final Map<String, RateLimiterConfiguration.KeyConfig> keyConfigs;
    private final Map<String, RateLimiterConfiguration.KeyConfig> patternConfigs;

    public ConfigurationResponse(int capacity, int refillRate, long cleanupIntervalMs, RateLimitAlgorithm algorithm,
                                Map<String, RateLimiterConfiguration.KeyConfig> keyConfigs,
                                Map<String, RateLimiterConfiguration.KeyConfig> patternConfigs) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.cleanupIntervalMs = cleanupIntervalMs;
        this.algorithm = algorithm;
        this.keyConfigs = keyConfigs != null ? new HashMap<>(keyConfigs) : new HashMap<>();
        this.patternConfigs = patternConfigs != null ? new HashMap<>(patternConfigs) : new HashMap<>();
    }

    public int getCapacity() {
        return capacity;
    }

    public int getRefillRate() {
        return refillRate;
    }

    public long getCleanupIntervalMs() {
        return cleanupIntervalMs;
    }

    public RateLimitAlgorithm getAlgorithm() {
        return algorithm;
    }

    public Map<String, RateLimiterConfiguration.KeyConfig> getKeyConfigs() {
        return new HashMap<>(keyConfigs);
    }

    public Map<String, RateLimiterConfiguration.KeyConfig> getPatternConfigs() {
        return new HashMap<>(patternConfigs);
    }
}