package com.sr.api_gateway_3.Config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) return false;
        Boolean exists = redisTemplate.hasKey("blacklist:" + jti);
        return Boolean.TRUE.equals(exists);
    }
}



