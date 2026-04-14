package dev.bnacar.distributedratelimiter.controller;

import dev.bnacar.distributedratelimiter.models.BenchmarkRequest;
import dev.bnacar.distributedratelimiter.models.BenchmarkResponse;
import dev.bnacar.distributedratelimiter.ratelimit.DistributedRateLimiterService;
import dev.bnacar.distributedratelimiter.ratelimit.RateLimiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import jakarta.validation.Valid;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Benchmark controller for measuring rate limiter performance.
 * Provides endpoints to test throughput under various load conditions.
 */
@RestController
@RequestMapping("/api/benchmark")
@Tag(name = "benchmark-controller", description = "Performance benchmarking and load testing utilities")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000", "http://[::1]:5173", "http://[::1]:3000"})
public class BenchmarkController {

    private final RateLimiterService rateLimiterService;
    private final DistributedRateLimiterService distributedRateLimiterService;
    private final ExecutorService benchmarkExecutor;

    public BenchmarkController(RateLimiterService rateLimiterService,
                               @org.springframework.beans.factory.annotation.Autowired(required = false)
                               DistributedRateLimiterService distributedRateLimiterService) {
        this.rateLimiterService = rateLimiterService;
        this.distributedRateLimiterService = distributedRateLimiterService;
        this.benchmarkExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "Benchmark-Worker");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Run a performance benchmark of the rate limiter.
     * Tests throughput under specified load conditions.
     */
    @PostMapping("/run")
    @Operation(summary = "Run performance benchmark",
               description = "Executes a performance benchmark with configurable load parameters to measure rate limiter throughput and latency")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Benchmark completed successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = BenchmarkResponse.class),
                                     examples = @ExampleObject(value = "{\"totalRequests\":1000,\"successCount\":850,\"errorCount\":0,\"durationSeconds\":10.5,\"throughputPerSecond\":95.2,\"successRate\":85.0,\"concurrentThreads\":10,\"requestsPerThread\":100}"))),
        @ApiResponse(responseCode = "400", 
                    description = "Benchmark configuration invalid or benchmark failed")
    })
    public ResponseEntity<BenchmarkResponse> runBenchmark(
            @Parameter(description = "Benchmark configuration parameters", required = true,
                      content = @Content(examples = @ExampleObject(value = "{\"concurrentThreads\":10,\"requestsPerThread\":100,\"durationSeconds\":30,\"keyPrefix\":\"benchmark\",\"tokensPerRequest\":1,\"delayBetweenRequestsMs\":0}")))
            @Valid @RequestBody BenchmarkRequest request) {
        long startTime = System.nanoTime();
        
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong errorCount = new AtomicLong(0);
        AtomicLong totalRequests = new AtomicLong(0);
        Map<String, KeyCounters> perKeyCounters = new ConcurrentHashMap<>();

        List<String> selectedKeys = resolveLoadTestKeys(request);
        
        CountDownLatch latch = new CountDownLatch(request.getConcurrentThreads());
        
        // Launch concurrent workers
        for (int i = 0; i < request.getConcurrentThreads(); i++) {
            final String key = selectedKeys.get(i % selectedKeys.size());
            final KeyCounters keyCounters = perKeyCounters.computeIfAbsent(key, ignored -> new KeyCounters());
            benchmarkExecutor.submit(() -> {
                try {
                    runWorkerThread(request, key, successCount, errorCount, totalRequests, keyCounters);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            // Wait for all workers to complete or timeout
            boolean completed = latch.await(request.getDurationSeconds() + 10, TimeUnit.SECONDS);
            if (!completed) {
                return ResponseEntity.badRequest().body(
                    BenchmarkResponse.error("Benchmark timed out")
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.badRequest().body(
                BenchmarkResponse.error("Benchmark interrupted")
            );
        }
        
        long endTime = System.nanoTime();
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
        
        long total = totalRequests.get();
        long success = successCount.get();
        long errors = errorCount.get();
        String backendUsed = resolveBackendUsed();
        Map<String, BenchmarkResponse.PerKeyResult> perKeyResults = buildPerKeyResults(request, perKeyCounters);
        
        double throughputPerSecond = total / durationSeconds;
        double successRate = total > 0 ? (double) success / total * 100.0 : 0.0;
        
        BenchmarkResponse response = new BenchmarkResponse(
            total,
            success,
            errors,
            durationSeconds,
            throughputPerSecond,
            successRate,
            request.getConcurrentThreads(),
            request.getRequestsPerThread(),
            backendUsed,
            perKeyResults
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Simple health check endpoint for the benchmark service.
     */
    @GetMapping("/health")
    @Operation(summary = "Benchmark service health check",
               description = "Check if the benchmark service is operational")
    @ApiResponse(responseCode = "200", 
                description = "Benchmark service is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Benchmark service is healthy");
    }
    
    private void runWorkerThread(BenchmarkRequest request,
                                String key,
                                AtomicLong successCount, AtomicLong errorCount, 
                                AtomicLong totalRequests,
                                KeyCounters keyCounters) {
        long requestsPerThread = request.getRequestsPerThread();
        int tokensPerRequest = request.getTokensPerRequest();
        
        long startTime = System.currentTimeMillis();
        long durationMs = request.getDurationSeconds() * 1000L;
        
        for (long i = 0; i < requestsPerThread; i++) {
            // Check if we've exceeded the duration
            if (System.currentTimeMillis() - startTime > durationMs) {
                break;
            }
            
            try {
                boolean allowed = distributedRateLimiterService != null
                    ? distributedRateLimiterService.isAllowed(key, tokensPerRequest)
                    : rateLimiterService.isAllowed(key, tokensPerRequest);
                totalRequests.incrementAndGet();
                keyCounters.totalRequests.incrementAndGet();
                
                if (allowed) {
                    successCount.incrementAndGet();
                    keyCounters.successfulRequests.incrementAndGet();
                } else {
                    keyCounters.throttledRequests.incrementAndGet();
                }
                
                // Optional delay between requests
                if (request.getDelayBetweenRequestsMs() > 0) {
                    Thread.sleep(request.getDelayBetweenRequestsMs());
                }
                
            } catch (Exception e) {
                totalRequests.incrementAndGet();
                keyCounters.totalRequests.incrementAndGet();
                errorCount.incrementAndGet();
                keyCounters.errorRequests.incrementAndGet();
            }
        }
    }

    private List<String> resolveLoadTestKeys(BenchmarkRequest request) {
        List<String> keys = new ArrayList<>();

        if (StringUtils.hasText(request.getStandardApiKey())) {
            keys.add(request.getStandardApiKey().trim());
        }
        if (StringUtils.hasText(request.getPremiumApiKey())) {
            String premiumKey = request.getPremiumApiKey().trim();
            if (!keys.contains(premiumKey)) {
                keys.add(premiumKey);
            }
        }

        if (keys.isEmpty()) {
            String keyPrefix = StringUtils.hasText(request.getKeyPrefix()) ? request.getKeyPrefix().trim() : "benchmark";
            keys.add(keyPrefix + ":single");
        }

        return keys;
    }

    private Map<String, BenchmarkResponse.PerKeyResult> buildPerKeyResults(BenchmarkRequest request,
                                                                            Map<String, KeyCounters> perKeyCounters) {
        Map<String, BenchmarkResponse.PerKeyResult> results = new LinkedHashMap<>();
        for (Map.Entry<String, KeyCounters> entry : perKeyCounters.entrySet()) {
            String key = entry.getKey();
            KeyCounters counters = entry.getValue();

            long total = counters.totalRequests.get();
            long successful = counters.successfulRequests.get();
            long throttled = counters.throttledRequests.get();
            long errors = counters.errorRequests.get();
            double successRate = total > 0 ? (double) successful / total * 100.0 : 0.0;

            results.put(key, new BenchmarkResponse.PerKeyResult(
                resolveUserType(request, key),
                total,
                successful,
                throttled,
                errors,
                successRate
            ));
        }
        return results;
    }

    private String resolveUserType(BenchmarkRequest request, String key) {
        if (StringUtils.hasText(request.getStandardApiKey()) && key.equals(request.getStandardApiKey().trim())) {
            return "STANDARD";
        }
        if (StringUtils.hasText(request.getPremiumApiKey()) && key.equals(request.getPremiumApiKey().trim())) {
            return "PREMIUM";
        }
        return "CUSTOM";
    }

    private static class KeyCounters {
        private final AtomicLong totalRequests = new AtomicLong();
        private final AtomicLong successfulRequests = new AtomicLong();
        private final AtomicLong throttledRequests = new AtomicLong();
        private final AtomicLong errorRequests = new AtomicLong();
    }

    private String resolveBackendUsed() {
        if (distributedRateLimiterService == null) {
            return "IN_MEMORY";
        }
        return distributedRateLimiterService.isUsingRedis() ? "REDIS" : "FALLBACK_IN_MEMORY";
    }
}