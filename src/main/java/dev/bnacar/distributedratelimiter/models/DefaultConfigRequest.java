package dev.bnacar.distributedratelimiter.models;

import dev.bnacar.distributedratelimiter.ratelimit.RateLimitAlgorithm;

public class DefaultConfigRequest {
    private Integer capacity;
    private Integer refillRate;
    private Long cleanupIntervalMs;
    private RateLimitAlgorithm algorithm;

    public DefaultConfigRequest() {}

    public DefaultConfigRequest(Integer capacity, Integer refillRate, Long cleanupIntervalMs, RateLimitAlgorithm algorithm) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.cleanupIntervalMs = cleanupIntervalMs;
        this.algorithm = algorithm;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(Integer refillRate) {
        this.refillRate = refillRate;
    }

    public Long getCleanupIntervalMs() {
        return cleanupIntervalMs;
    }

    public void setCleanupIntervalMs(Long cleanupIntervalMs) {
        this.cleanupIntervalMs = cleanupIntervalMs;
    }

    public RateLimitAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(RateLimitAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
}