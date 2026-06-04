package dev.bnacar.distributedratelimiter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.bnacar.distributedratelimiter.config.SecurityConfiguration;
import dev.bnacar.distributedratelimiter.models.RateLimitRequest;
import dev.bnacar.distributedratelimiter.monitoring.MetricsService;
import dev.bnacar.distributedratelimiter.ratelimit.DistributedRateLimiterService;
import dev.bnacar.distributedratelimiter.ratelimit.RateLimiterService;
import dev.bnacar.distributedratelimiter.security.ApiKeyService;
import dev.bnacar.distributedratelimiter.security.IpAddressExtractor;
import dev.bnacar.distributedratelimiter.security.IpSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RateLimitController.class)
@Import(RateLimitControllerTest.TestConfig.class)
class RateLimitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DistributedRateLimiterService distributedRateLimiterService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private IpSecurityService ipSecurityService;

    @Autowired
    private IpAddressExtractor ipAddressExtractor;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @BeforeEach
    void setUp() {
        SecurityConfiguration.Headers headers = new SecurityConfiguration.Headers();
        headers.setEnabled(true);

        when(securityConfiguration.getHeaders()).thenReturn(headers);
        when(securityConfiguration.getMaxRequestSize()).thenReturn("1MB");
        when(ipAddressExtractor.getClientIpAddress(any())).thenReturn("127.0.0.1");
        when(ipSecurityService.isIpAllowed("127.0.0.1")).thenReturn(true);
        when(apiKeyService.isValidApiKey(null)).thenReturn(true);
        when(apiKeyService.getApiKeyTier(null)).thenReturn("standard");
        when(ipSecurityService.createIpBasedKey("user1", "127.0.0.1")).thenReturn("ip:127.0.0.1:user1");
    }

    @Test
    void checkRateLimitReturnsOkWhenAllowed() throws Exception {
        when(distributedRateLimiterService.isAllowed("ip:127.0.0.1:user1", 5)).thenReturn(true);

        RateLimitRequest request = new RateLimitRequest("user1", 5);

        mockMvc.perform(post("/api/ratelimit/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("user1"))
            .andExpect(jsonPath("$.tokensRequested").value(5))
            .andExpect(jsonPath("$.allowed").value(true));
    }

    @Test
    void checkRateLimitReturnsTooManyRequestsWhenDenied() throws Exception {
        when(distributedRateLimiterService.isAllowed("ip:127.0.0.1:user1", 5)).thenReturn(false);

        RateLimitRequest request = new RateLimitRequest("user1", 5);

        mockMvc.perform(post("/api/ratelimit/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.key").value("user1"))
            .andExpect(jsonPath("$.tokensRequested").value(5))
            .andExpect(jsonPath("$.allowed").value(false));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        RateLimiterService rateLimiterService() {
            return mock(RateLimiterService.class);
        }

        @Bean
        DistributedRateLimiterService distributedRateLimiterService() {
            return mock(DistributedRateLimiterService.class);
        }

        @Bean
        MetricsService metricsService() {
            return mock(MetricsService.class);
        }

        @Bean
        ApiKeyService apiKeyService() {
            return mock(ApiKeyService.class);
        }

        @Bean
        IpSecurityService ipSecurityService() {
            return mock(IpSecurityService.class);
        }

        @Bean
        IpAddressExtractor ipAddressExtractor() {
            return mock(IpAddressExtractor.class);
        }

        @Bean
        SecurityConfiguration securityConfiguration() {
            return mock(SecurityConfiguration.class);
        }
    }
}
