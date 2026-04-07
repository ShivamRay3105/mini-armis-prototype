package com.sr.AUTH_SERVICE.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    // THE UNIVERSAL PREFIX FOR AUTH SERVICE
    private static final String AUTH_PREFIX = "AUTH:BLACKLIST:";

    public void blacklistToken(String token, long expirationMillis) {
        String key = AUTH_PREFIX + token;
        // We store the token as the key and "true" as the value
        redisTemplate.opsForValue().set(key, "true", expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        String key = AUTH_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}