package com.airfryer.repicka.common.configuration;

import com.airfryer.repicka.common.redis.dto.KeyExpiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {
    private final RedisProperties redisProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
    }
    
    // Redis 메시지 리스너 컨테이너
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
    
    // 키 만료 이벤트 리스너 컨테이너
    @Bean
    public KeyExpirationEventMessageListener keyExpirationEventMessageListener(
            RedisMessageListenerContainer redisMessageListenerContainer) {
        
        return new KeyExpirationEventMessageListener(redisMessageListenerContainer) {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                String expiredKey = message.toString();
                
                // 키 만료 이벤트 발행
                eventPublisher.publishEvent(new KeyExpiredEvent(this, expiredKey));
            }
        };
    }
    
    // Redis Keyspace Notifications 자동 활성화 - 애플리케이션 컨텍스트 초기화 완료 후 실행
    @EventListener(ContextRefreshedEvent.class)
    public void enableKeyspaceNotifications(ContextRefreshedEvent event) {
        try {
            StringRedisTemplate stringRedisTemplate = event.getApplicationContext().getBean(StringRedisTemplate.class);
            // Redis Keyspace Notifications 활성화 (키 만료 이벤트)
            stringRedisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Void>) connection -> {
                connection.serverCommands().setConfig("notify-keyspace-events", "Ex");
                log.info("Redis Keyspace Notifications 활성화됨: Ex (키 만료 이벤트)");
                return null;
            });
        } catch (Exception e) {
            log.warn("Redis Keyspace Notifications 활성화 실패 - 수동으로 설정하세요: CONFIG SET notify-keyspace-events Ex", e);
        }
    }
}
