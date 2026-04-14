package dev.bnacar.distributedratelimiter.security;

import dev.bnacar.distributedratelimiter.config.SecurityConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
public class ApiKeyService {

    private final SecurityConfiguration securityConfiguration;
    private final ApiKeyManagementService apiKeyManagementService;

    @Autowired
    public ApiKeyService(SecurityConfiguration securityConfiguration,
                         @Autowired(required = false) ApiKeyManagementService apiKeyManagementService) {
        this.securityConfiguration = securityConfiguration;
        this.apiKeyManagementService = apiKeyManagementService;
    }

    /**
     * Validates an API key if API key authentication is enabled
     * @param apiKey the API key to validate
     * @return true if the API key is valid or if API key authentication is disabled, false otherwise
     */
    public boolean isValidApiKey(String apiKey) {
        if (!securityConfiguration.getApiKeys().isEnabled()) {
            return true; // API key validation is disabled
        }

        if (!StringUtils.hasText(apiKey)) {
            return false; // API key is required when enabled
        }

        if (!securityConfiguration.getApiKeys().getValidKeys().contains(apiKey)) {
            return false;
        }

        if (apiKeyManagementService == null) {
            return true;
        }

        return apiKeyManagementService.isKeyActive(apiKey);
    }

    /**
     * Checks if API key authentication is required
     * @return true if API key authentication is enabled
     */
    public boolean isApiKeyRequired() {
        return securityConfiguration.getApiKeys().isEnabled();
    }

    public String getApiKeyTier(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return "ANONYMOUS";
        }

        String normalized = apiKey.toLowerCase(Locale.ROOT);
        if (normalized.contains("premium")) {
            return "PREMIUM";
        }

        return "STANDARD";
    }
}