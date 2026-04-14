package dev.bnacar.distributedratelimiter.controller;

import dev.bnacar.distributedratelimiter.models.ApiKeyListResponse;
import dev.bnacar.distributedratelimiter.security.ApiKeyManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/keys")
@Tag(name = "API Keys", description = "API key management operations")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000", "http://[::1]:5173", "http://[::1]:3000"})
public class ApiKeyManagementController {

    private final ApiKeyManagementService apiKeyManagementService;

    public ApiKeyManagementController(ApiKeyManagementService apiKeyManagementService) {
        this.apiKeyManagementService = apiKeyManagementService;
    }

    @GetMapping
    @Operation(summary = "List API keys with status and usage")
    @ApiResponse(responseCode = "200", description = "API keys retrieved")
    public ResponseEntity<ApiKeyListResponse> listApiKeys() {
        return ResponseEntity.ok(apiKeyManagementService.listApiKeys());
    }

    @PostMapping("/{key}/activate")
    @Operation(summary = "Activate API key")
    @ApiResponse(responseCode = "200", description = "API key activated")
    @ApiResponse(responseCode = "400", description = "Invalid key")
    public ResponseEntity<String> activateApiKey(@PathVariable("key") String key) {
        if (!apiKeyManagementService.activateKey(key)) {
            return ResponseEntity.badRequest().body("Invalid API key");
        }
        return ResponseEntity.ok("API key activated: " + key);
    }

    @PostMapping("/{key}/deactivate")
    @Operation(summary = "Deactivate API key")
    @ApiResponse(responseCode = "200", description = "API key deactivated")
    @ApiResponse(responseCode = "400", description = "Invalid key")
    public ResponseEntity<String> deactivateApiKey(@PathVariable("key") String key) {
        if (!apiKeyManagementService.deactivateKey(key)) {
            return ResponseEntity.badRequest().body("Invalid API key");
        }
        return ResponseEntity.ok("API key deactivated: " + key);
    }
}

