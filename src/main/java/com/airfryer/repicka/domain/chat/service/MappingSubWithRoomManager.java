package com.airfryer.repicka.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MappingSubWithRoomManager
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

    public Map<String, Long> getAllMappingsBySessionId(String sessionId)
    {
        String pattern = buildKey(sessionId, "*");
        Set<String> keys = redisTemplate.keys(pattern);

        if(keys.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Long> result = new HashMap<>();

        for(String key : keys)
        {
            String value = (String) redisTemplate.opsForValue().get(key);

            if (value != null)
            {
                String[] parts = key.split(":");
                String subId = parts[parts.length - 1];
                result.put(subId, Long.parseLong(value));
            }
        }

        return result;
    }

    // 매핑 정보 제거
    public void removeMapping(String sessionId, String subId)
    {
        String key = buildKey(sessionId, subId);
        redisTemplate.delete(key);
    }

    public void removeAllMappingsBySessionId(String sessionId)
    {
        String pattern = buildKey(sessionId, "*");
        Set<String> keys = redisTemplate.keys(pattern);

        if(!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // key 생성
    private String buildKey(String sessionId, String subId) {
        return "chatroom:subscribe:" + sessionId + ":" + subId;
    }
}
