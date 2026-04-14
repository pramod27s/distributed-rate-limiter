package dev.bnacar.distributedratelimiter.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Response model for benchmark operations.
 */
public class BenchmarkResponse {
    
    private final boolean success;
    private final String errorMessage;
    private final long totalRequests;
    private final long successfulRequests;
    private final long errorRequests;
    private final double durationSeconds;
    private final double throughputPerSecond;
    private final double successRate;
    private final int concurrentThreads;
    private final long requestsPerThread;
    private final String backendUsed;
    private final Map<String, PerKeyResult> perKeyResults;
    
    // Constructor for successful benchmark
    @JsonCreator
    public BenchmarkResponse(
            @JsonProperty("totalRequests") long totalRequests, 
            @JsonProperty("successfulRequests") long successfulRequests, 
            @JsonProperty("errorRequests") long errorRequests,
            @JsonProperty("durationSeconds") double durationSeconds, 
            @JsonProperty("throughputPerSecond") double throughputPerSecond, 
            @JsonProperty("successRate") double successRate,
            @JsonProperty("concurrentThreads") int concurrentThreads, 
            @JsonProperty("requestsPerThread") long requestsPerThread,
            @JsonProperty("backendUsed") String backendUsed,
            @JsonProperty("perKeyResults") Map<String, PerKeyResult> perKeyResults) {
        this.success = true;
        this.errorMessage = null;
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.errorRequests = errorRequests;
        this.durationSeconds = durationSeconds;
        this.throughputPerSecond = throughputPerSecond;
        this.successRate = successRate;
        this.concurrentThreads = concurrentThreads;
        this.requestsPerThread = requestsPerThread;
        this.backendUsed = backendUsed;
        this.perKeyResults = perKeyResults != null ? new HashMap<>(perKeyResults) : new HashMap<>();
    }
    
    // Constructor for error response
    private BenchmarkResponse(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
        this.totalRequests = 0;
        this.successfulRequests = 0;
        this.errorRequests = 0;
        this.durationSeconds = 0;
        this.throughputPerSecond = 0;
        this.successRate = 0;
        this.concurrentThreads = 0;
        this.requestsPerThread = 0;
        this.backendUsed = null;
        this.perKeyResults = new HashMap<>();
    }
    
    public static BenchmarkResponse error(String errorMessage) {
        return new BenchmarkResponse(errorMessage);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public long getTotalRequests() {
        return totalRequests;
    }
    
    public long getSuccessfulRequests() {
        return successfulRequests;
    }
    
    public long getErrorRequests() {
        return errorRequests;
    }
    
    public double getDurationSeconds() {
        return durationSeconds;
    }
    
    public double getThroughputPerSecond() {
        return throughputPerSecond;
    }
    
    public double getSuccessRate() {
        return successRate;
    }
    
    public int getConcurrentThreads() {
        return concurrentThreads;
    }
    
    public long getRequestsPerThread() {
        return requestsPerThread;
    }

    public String getBackendUsed() {
        return backendUsed;
    }

    public Map<String, PerKeyResult> getPerKeyResults() {
        return new HashMap<>(perKeyResults);
    }

    public static class PerKeyResult {
        private final String userType;
        private final long totalRequests;
        private final long successfulRequests;
        private final long throttledRequests;
        private final long errorRequests;
        private final double successRate;

        @JsonCreator
        public PerKeyResult(
                @JsonProperty("userType") String userType,
                @JsonProperty("totalRequests") long totalRequests,
                @JsonProperty("successfulRequests") long successfulRequests,
                @JsonProperty("throttledRequests") long throttledRequests,
                @JsonProperty("errorRequests") long errorRequests,
                @JsonProperty("successRate") double successRate) {
            this.userType = userType;
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.throttledRequests = throttledRequests;
            this.errorRequests = errorRequests;
            this.successRate = successRate;
        }

        public String getUserType() {
            return userType;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getSuccessfulRequests() {
            return successfulRequests;
        }

        public long getThrottledRequests() {
            return throttledRequests;
        }

        public long getErrorRequests() {
            return errorRequests;
        }

        public double getSuccessRate() {
            return successRate;
        }
    }
    
    /**
     * Check if the benchmark meets the performance target.
     */
    public boolean meetsPerformanceTarget(double targetThroughput) {
        return success && throughputPerSecond >= targetThroughput;
    }
    
    @Override
    public String toString() {
        if (!success) {
            return "BenchmarkResponse{success=false, error='" + errorMessage + "'}";
        }
        
        return String.format(
            "BenchmarkResponse{success=%s, totalRequests=%d, successfulRequests=%d, " +
            "errorRequests=%d, durationSeconds=%.2f, throughputPerSecond=%.2f, " +
            "successRate=%.2f%%, concurrentThreads=%d, requestsPerThread=%d, backendUsed=%s}",
            success, totalRequests, successfulRequests, errorRequests, 
            durationSeconds, throughputPerSecond, successRate, concurrentThreads, requestsPerThread, backendUsed
        );
    }
}