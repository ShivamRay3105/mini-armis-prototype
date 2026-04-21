package com.sr.asset_service.service;

import com.sr.asset_service.model.Asset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AssetCacheService {

    private final StringRedisTemplate redis;

    private static final String PREFIX = "asset:";
    private static final String ACTIVE_SET = "active_assets";
    private static final String RISK_ZSET = "high_risk_assets";
    private static final long TTL = 3600L;

    public void cacheAsset(Asset asset) {
        String key = PREFIX + asset.getMacAddress();

        Map<String, String> map = new HashMap<>();
        map.put("id", asset.getAssetId().toString());
        map.put("ipAddress", nullSafe(asset.getIpAddress()));
        map.put("deviceType", asset.getDeviceType() != null
                ? asset.getDeviceType().name()
                : "UNKNOWN");
        map.put("riskScore", String.valueOf(asset.getRiskScore()));
        map.put("riskLevel", asset.getRiskLevel().name());
        map.put("lastSeen", asset.getLastSeen().toString());
        map.put("communicationCount", String.valueOf(asset.getCommunicationCount()));
        map.put("status", asset.getStatus().name());

        redis.opsForHash().putAll(key, map);

        redis.expire(key, Duration.ofSeconds(TTL));

        redis.opsForSet().add(ACTIVE_SET, asset.getMacAddress());
        redis.opsForZSet().add(RISK_ZSET, asset.getMacAddress(), asset.getRiskScore());
    }

    public Optional<Map<Object, Object>> getCachedAsset(String macAddress) {
        String key = PREFIX + macAddress;
        Map<Object, Object> data = redis.opsForHash().entries(key);
        return data.isEmpty() ? Optional.empty() : Optional.of(data);
    }

    public boolean isCached(String macAddress) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + macAddress));
    }

    public void updateLastSeen(String macAddress) {
        String key = PREFIX + macAddress;
        if (Boolean.TRUE.equals(redis.hasKey(key))) {
            redis.opsForHash().put(key, "lastSeen", Instant.now().toString());
            redis.expire(key, Duration.ofSeconds(TTL)); // reset TTL
        }
    }

    public void updateRiskInCache(String macAddress, int riskScore, String riskLevel) {
        String key = PREFIX + macAddress;
        if (Boolean.TRUE.equals(redis.hasKey(key))) {
            redis.opsForHash().put(key, "riskScore", String.valueOf(riskScore));
            redis.opsForHash().put(key, "riskLevel", riskLevel);
            redis.opsForZSet().add(RISK_ZSET, macAddress, riskScore);
        }
    }

    private String nullSafe(Object val) {
        return val != null ? val.toString() : "";
    }
}