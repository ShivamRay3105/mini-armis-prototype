package com.sr.RISK_ENGINE.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sr.RISK_ENGINE.State.AssetContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, AssetContext> assetContextRedisTemplate(RedisConnectionFactory connectionFactory) {
        // 1. Create and configure the ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // IMPORTANT: Register JavaTimeModule so 'Instant' is serialized correctly
        mapper.registerModule(new JavaTimeModule());

        // This helps Jackson handle polymorphic types (like Deque/List) correctly
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // 2. Use the NON-DEPRECATED constructor (Passing the mapper)
        Jackson2JsonRedisSerializer<AssetContext> serializer = new Jackson2JsonRedisSerializer<>(mapper,
                AssetContext.class);

        RedisTemplate<String, AssetContext> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key: String (e.g., "asset:123")
        template.setKeySerializer(new StringRedisSerializer());

        // Value: JSON (AssetContext object)
        template.setValueSerializer(serializer);

        return template;
    }
}