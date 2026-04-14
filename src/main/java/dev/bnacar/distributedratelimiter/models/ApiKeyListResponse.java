package dev.bnacar.distributedratelimiter.models;

import java.util.ArrayList;
import java.util.List;

public class ApiKeyListResponse {
    private final List<ApiKeyInfoResponse> keys;
    private final int totalKeys;
    private final int activeKeys;

    public ApiKeyListResponse(List<ApiKeyInfoResponse> keys, int totalKeys, int activeKeys) {
        this.keys = keys != null ? new ArrayList<>(keys) : new ArrayList<>();
        this.totalKeys = totalKeys;
        this.activeKeys = activeKeys;
    }

    public List<ApiKeyInfoResponse> getKeys() {
        return new ArrayList<>(keys);
    }

    public int getTotalKeys() {
        return totalKeys;
    }

    public int getActiveKeys() {
        return activeKeys;
    }
}

