package com.airfryer.repicka.common.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class ChatConfig implements WebSocketMessageBrokerConfigurer
{
    // STOMP 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry)
    {
        // STOMP 접속 주소 URL = ws://localhost:8080/ws
        // 프로토콜이 http가 아님에 유의
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");
    }

    // 브로커 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry)
    {
        // 메시지를 발행하는 엔드포인트
        registry.setApplicationDestinationPrefixes("/pub");

        // 메시지를 구독하는 요청 엔드포인트
        registry.enableSimpleBroker("/sub");
    }
}
