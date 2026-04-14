package dev.bnacar.distributedratelimiter.security;

import dev.bnacar.distributedratelimiter.config.SecurityConfiguration;
import dev.bnacar.distributedratelimiter.models.ApiKeyInfoResponse;
import dev.bnacar.distributedratelimiter.models.ApiKeyListResponse;
import dev.bnacar.distributedratelimiter.models.ApiKeyUsageStatsResponse;
import dev.bnacar.distributedratelimiter.models.MetricsResponse;
import dev.bnacar.distributedratelimiter.monitoring.MetricsService;
import dev.bnacar.distributedratelimiter.ratelimit.RateLimitConfig;
import dev.bnacar.distributedratelimiter.ratelimit.RateLimiterConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiKeyManagementService {

    private final SecurityConfiguration securityConfiguration;
    private final RateLimiterConfiguration rateLimiterConfiguration;
    private final MetricsService metricsService;
    private final ConcurrentHashMap<String, Boolean> activationState = new ConcurrentHashMap<>();

    public ApiKeyManagementService(SecurityConfiguration securityConfiguration,
                                   RateLimiterConfiguration rateLimiterConfiguration,
                                   MetricsService metricsService) {
        this.securityConfiguration = securityConfiguration;
        this.rateLimiterConfiguration = rateLimiterConfiguration;
        this.metricsService = metricsService;
    }

    public ApiKeyListResponse listApiKeys() {
        MetricsResponse metrics = metricsService.getMetrics();
        Set<String> apiKeys = new LinkedHashSet<>();

        apiKeys.addAll(securityConfiguration.getApiKeys().getValidKeys());
        apiKeys.addAll(rateLimiterConfiguration.getKeys().keySet());
        apiKeys.addAll(metrics.getKeyMetrics().keySet());

        List<ApiKeyInfoResponse> response = new ArrayList<>();
        int activeCount = 0;

        for (String key : apiKeys) {
            boolean isActive = isKeyActive(key);
            if (isActive) {
                activeCount++;
            }

            RateLimitConfig config = resolveKeyConfig(key);
            MetricsResponse.KeyMetrics keyMetrics = metrics.getKeyMetrics().get(key);
            long allowed = keyMetrics != null ? keyMetrics.getAllowedRequests() : 0;
            long denied = keyMetrics != null ? keyMetrics.getDeniedRequests() : 0;
            long lastUsedAt = keyMetrics != null ? keyMetrics.getLastAccessTime() : 0;

            ApiKeyUsageStatsResponse usage = new ApiKeyUsageStatsResponse(
                allowed + denied,
                allowed,
                denied
            );

            response.add(new ApiKeyInfoResponse(
                key,
                resolveDisplayName(key),
                resolveDescription(key, config),
                inferUserType(key),
                isActive,
                config.getCapacity(),
                config.getRefillRate(),
                config.getAlgorithm().name(),
                lastUsedAt,
                usage
            ));
        }

        return new ApiKeyListResponse(response, response.size(), activeCount);
    }

    public boolean isKeyActive(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        return activationState.computeIfAbsent(apiKey, this::defaultActivation);
    }

    public boolean activateKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        activationState.put(apiKey, true);
        return true;
    }

    public boolean deactivateKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        activationState.put(apiKey, false);
        return true;
    }

    private boolean defaultActivation(String apiKey) {
        return securityConfiguration.getApiKeys().getValidKeys().contains(apiKey);
    }

    private RateLimitConfig resolveKeyConfig(String apiKey) {
        RateLimiterConfiguration.KeyConfig keyConfig = rateLimiterConfiguration.getKeys().get(apiKey);
        if (keyConfig == null) {
            return rateLimiterConfiguration.getDefaultConfig();
        }

        int capacity = keyConfig.getCapacity() > 0 ? keyConfig.getCapacity() : rateLimiterConfiguration.getCapacity();
        int refillRate = keyConfig.getRefillRate() > 0 ? keyConfig.getRefillRate() : rateLimiterConfiguration.getRefillRate();
        long cleanupInterval = keyConfig.getCleanupIntervalMs() != null && keyConfig.getCleanupIntervalMs() > 0
            ? keyConfig.getCleanupIntervalMs()
            : rateLimiterConfiguration.getCleanupIntervalMs();

        return new RateLimitConfig(
            capacity,
            refillRate,
            cleanupInterval,
            keyConfig.getAlgorithm() != null ? keyConfig.getAlgorithm() : rateLimiterConfiguration.getAlgorithm()
        );
    }

    private String resolveDisplayName(String apiKey) {
        RateLimiterConfiguration.KeyConfig keyConfig = rateLimiterConfiguration.getKeys().get(apiKey);
        if (keyConfig != null && keyConfig.getDisplayName() != null && !keyConfig.getDisplayName().trim().isEmpty()) {
            return keyConfig.getDisplayName().trim();
        }
        return apiKey;
    }

    private String resolveDescription(String apiKey, RateLimitConfig config) {
        RateLimiterConfiguration.KeyConfig keyConfig = rateLimiterConfiguration.getKeys().get(apiKey);
        if (keyConfig != null && keyConfig.getDescription() != null && !keyConfig.getDescription().trim().isEmpty()) {
            return keyConfig.getDescription().trim();
        }
        return "Managed API key (" + config.getAlgorithm().name() + ")";
    }

    private String inferUserType(String apiKey) {
        String normalized = apiKey.toLowerCase();
        if (normalized.contains("premium")) {
            return "PREMIUM";
        }
        return "STANDARD";
    }
}

