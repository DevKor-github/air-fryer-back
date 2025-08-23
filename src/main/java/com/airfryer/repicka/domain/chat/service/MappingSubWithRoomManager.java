package com.airfryer.repicka.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MappingSubWithRoomManager
{
    private final StringRedisTemplate redisTemplate;

    // (세션 ID + 구독 ID) -> 채팅방 ID 매핑 정보 생성(갱신)
    public void set(String sessionId, String subId, Long chatRoomId)
    {
        String key = buildKey(sessionId, subId);
        redisTemplate.opsForValue().set(key, chatRoomId.toString());
    }

    // 세션 ID, 구독 ID로 채팅방 ID 조회
    public Long get(String sessionId, String subId)
    {
        String key = buildKey(sessionId, subId);
        String value = redisTemplate.opsForValue().get(key);

        return value != null ? Long.parseLong(value) : null;
    }

    // 세션 ID로 채팅방 ID 리스트 조회
    public List<Long> get(String sessionId)
    {
        String pattern = buildKey(sessionId, "*");
        Set<String> keys = redisTemplate.keys(pattern);

        if(keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> result = new ArrayList<>();

        for(String key : keys)
        {
            String value = redisTemplate.opsForValue().get(key);

            if(value != null) {
                result.add(Long.parseLong(value));
            }
        }

        return result;
    }

    // 세션 ID, 구독 ID로 매핑 정보 제거
    public void delete(String sessionId, String subId)
    {
        String key = buildKey(sessionId, subId);
        redisTemplate.delete(key);
    }

    // 세션 ID로 모든 매핑 정보 제거
    public void delete(String sessionId)
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
