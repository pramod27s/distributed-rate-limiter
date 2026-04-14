package dev.bnacar.distributedratelimiter.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class MetricsResponse {
    private final Map<String, KeyMetrics> keyMetrics;
    private final boolean redisConnected;
    private final long totalAllowedRequests;
    private final long totalDeniedRequests;
    private final List<RequestEvent> recentEvents;

    @JsonCreator
    public MetricsResponse(@JsonProperty("keyMetrics") Map<String, KeyMetrics> keyMetrics, 
                          @JsonProperty("redisConnected") boolean redisConnected, 
                          @JsonProperty("totalAllowedRequests") long totalAllowedRequests, 
                          @JsonProperty("totalDeniedRequests") long totalDeniedRequests,
                          @JsonProperty("recentEvents") List<RequestEvent> recentEvents) {
        this.keyMetrics = keyMetrics != null ? new HashMap<>(keyMetrics) : new HashMap<>();
        this.redisConnected = redisConnected;
        this.totalAllowedRequests = totalAllowedRequests;
        this.totalDeniedRequests = totalDeniedRequests;
        this.recentEvents = recentEvents != null ? new ArrayList<>(recentEvents) : new ArrayList<>();
    }

    public Map<String, KeyMetrics> getKeyMetrics() {
        return new HashMap<>(keyMetrics);
    }

    public boolean isRedisConnected() {
        return redisConnected;
    }

    public long getTotalAllowedRequests() {
        return totalAllowedRequests;
    }

    public long getTotalDeniedRequests() {
        return totalDeniedRequests;
    }

    public List<RequestEvent> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }

    public static class RequestEvent {
        private final String id;
        private final long timestamp;
        private final String key;
        private final String apiKey;
        private final String userType;
        private final String algorithm;
        private final int tokensRequested;
        private final boolean allowed;

        @JsonCreator
        public RequestEvent(
                @JsonProperty("id") String id,
                @JsonProperty("timestamp") long timestamp,
                @JsonProperty("key") String key,
                @JsonProperty("apiKey") String apiKey,
                @JsonProperty("userType") String userType,
                @JsonProperty("algorithm") String algorithm,
                @JsonProperty("tokensRequested") int tokensRequested,
                @JsonProperty("allowed") boolean allowed) {
            this.id = id;
            this.timestamp = timestamp;
            this.key = key;
            this.apiKey = apiKey;
            this.userType = userType;
            this.algorithm = algorithm;
            this.tokensRequested = tokensRequested;
            this.allowed = allowed;
        }

        public String getId() {
            return id;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getKey() {
            return key;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getUserType() {
            return userType;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public int getTokensRequested() {
            return tokensRequested;
        }

        public boolean isAllowed() {
            return allowed;
        }
    }

    public static class KeyMetrics {
        private final long allowedRequests;
        private final long deniedRequests;
        private final long lastAccessTime;

        @JsonCreator
        public KeyMetrics(@JsonProperty("allowedRequests") long allowedRequests, 
                         @JsonProperty("deniedRequests") long deniedRequests, 
                         @JsonProperty("lastAccessTime") long lastAccessTime) {
            this.allowedRequests = allowedRequests;
            this.deniedRequests = deniedRequests;
            this.lastAccessTime = lastAccessTime;
        }

        public long getAllowedRequests() {
            return allowedRequests;
        }

        public long getDeniedRequests() {
            return deniedRequests;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }
    }
}