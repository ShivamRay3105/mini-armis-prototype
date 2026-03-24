package com.sr.RISK_ENGINE.Service.Impl;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.Service.IdempotencyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "event_id:";

    @Override
    public boolean isDuplicate(UUID eventId) {
        String key = KEY_PREFIX + eventId.toString();
        // .setIfAbsent is the Java version of the Redis "SETNX" command.
        // It tries to create the key only if it doesn't exist.
        // We set a value of "1" and a TTL of 24 hours.
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(
                key,
                "1",
                Duration.ofHours(1));

        // If isNew is false, it means the key already existed -> It's a duplicate!
        return Boolean.FALSE.equals(isNew);
    }
}
