package com.airfryer.repicka.common.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {
    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        
        // String Serializer for keys
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // JSON Serializer for values (Object 저장)
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        
        // Key Serializers
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        
        // Value Serializers
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    
    // Redis 키 만료 이벤트를 위한 메시지 리스너 컨테이너
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        return container;
    }
    
    // Redis Keyspace Notifications 자동 활성화
    @PostConstruct
    public void enableKeyspaceNotifications() {
        try {
            RedisTemplate<String, Object> template = redisTemplate();
            // Redis Keyspace Notifications 활성화 (키 만료 이벤트)
            template.execute((org.springframework.data.redis.core.RedisCallback<Void>) connection -> {
                connection.serverCommands().setConfig("notify-keyspace-events", "Ex");
                log.info("Redis Keyspace Notifications 활성화됨: Ex (키 만료 이벤트)");
                return null;
            });
        } catch (Exception e) {
            log.warn("Redis Keyspace Notifications 활성화 실패 - 수동으로 설정하세요: CONFIG SET notify-keyspace-events Ex", e);
        }
    }
}
