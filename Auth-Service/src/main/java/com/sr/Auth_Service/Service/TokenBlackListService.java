package com.sr.Auth_Service.Service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class TokenBlackListService {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlackListService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String jti, Date expiration) {
        if (jti == null || jti.isBlank() || expiration == null)
            return;
        long ttlSeconds = Math.max(0, (expiration.getTime() - System.currentTimeMillis()) / 1000);
        redisTemplate.opsForValue().set("blacklist:" + jti, "1", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank())
            return false;
        Boolean exists = redisTemplate.hasKey("blacklist:" + jti);
        return Boolean.TRUE.equals(exists);
    }
}
