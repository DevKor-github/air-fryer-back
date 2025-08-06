package com.airfryer.repicka.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapSubscribeWithRoomManager
{
    private final RedisTemplate<String, Object> redisTemplate;

    // (sessionId + subscriptionId) -> chatRoomId 매핑 정보 생성(갱신)
    public void mapSubscribeWithRoom(String sessionId, String subId, Long chatRoomId)
    {
        String key = buildKey(sessionId, subId);
        redisTemplate.opsForValue().set(key, chatRoomId.toString());
    }

    // (sessionId + subscriptionId) -> chatRoomId 매핑 정보 조회
    public Long getChatRoomIdBySubId(String sessionId, String subId)
    {
        String key = buildKey(sessionId, subId);
        String value = (String) redisTemplate.opsForValue().get(key);

        return value != null ? Long.parseLong(value) : null;
    }

    // 매핑 정보 제거
    public void removeMapping(String sessionId, String subId)
    {
        String key = buildKey(sessionId, subId);
        redisTemplate.delete(key);
    }

    // key 생성
    private String buildKey(String sessionId, String subId) {
        return "chatroom:subscribe:" + sessionId + ":" + subId;
    }
}
