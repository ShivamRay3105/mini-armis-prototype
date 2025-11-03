package com.sr.Auth_Service.Service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    public RefreshTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String mintRefreshToken(String userId, Duration ttl) {
        String refreshToken = UUID.randomUUID().toString();
        String key = key(userId, refreshToken);
        redisTemplate.opsForValue().set(key, "1", ttl);
        return refreshToken;
    }

    public boolean validate(String userId, String refreshToken) {
        String key = key(userId, refreshToken);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    public void revoke(String userId, String refreshToken) {
        redisTemplate.delete(key(userId, refreshToken));
    }

    public void revokeAllForUser(String userId) {
        // Optional: store a user-level version instead for O(1) revocation
        // For simplicity, we rely on caller to pass the specific token to delete.
    }

    private String key(String userId, String refreshToken) {
        return "refresh:" + userId + ":" + Optional.ofNullable(refreshToken).orElse("");
    }
}


