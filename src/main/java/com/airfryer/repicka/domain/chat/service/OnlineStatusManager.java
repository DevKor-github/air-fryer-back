package com.airfryer.repicka.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OnlineStatusManager
{
    private final RedisTemplate<String, Object> redisTemplate;

    // 채팅방 입장
    public void markUserOnline(Long chatRoomId, Long userId)
    {
        String key = buildKey(chatRoomId, userId);
        redisTemplate.opsForValue().set(key, "true");
    }

    // 채팅방 퇴장
    public void markUserOffline(Long chatRoomId, Long userId)
    {
        String key = buildKey(chatRoomId, userId);
        redisTemplate.delete(key);
    }

    // 온라인 여부 조회
    public boolean isUserOnline(Long chatRoomId, Long userId)
    {
        String key = buildKey(chatRoomId, userId);

        return redisTemplate.hasKey(key);
    }

    // key 생성
    private String buildKey(Long chatRoomId, Long userId) {
        return "chatroom:online:" + chatRoomId + ":" + userId;
    }
}
