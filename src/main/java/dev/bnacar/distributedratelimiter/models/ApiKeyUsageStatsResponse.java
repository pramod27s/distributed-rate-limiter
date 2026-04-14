package dev.bnacar.distributedratelimiter.models;

public class ApiKeyUsageStatsResponse {
    private final long totalRequests;
    private final long successfulRequests;
    private final long rateLimitedRequests;

    public ApiKeyUsageStatsResponse(long totalRequests, long successfulRequests, long rateLimitedRequests) {
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.rateLimitedRequests = rateLimitedRequests;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getSuccessfulRequests() {
        return successfulRequests;
    }

    public long getRateLimitedRequests() {
        return rateLimitedRequests;
    }
}

