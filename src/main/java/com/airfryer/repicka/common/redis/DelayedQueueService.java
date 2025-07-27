package com.airfryer.repicka.common.redis;

import com.airfryer.repicka.common.redis.dto.AppointmentTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class DelayedQueueService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String DELAYED_TASK_PREFIX = "delayed:task:";
    private static final String TASK_DATA_PREFIX = "taskdata:";
    
    // 지연 작업을 Redis TTL로 등록
    public boolean addDelayedTask(String queueName, AppointmentTask taskData, LocalDateTime executeAt) {
        try {
            // 의미있는 작업 ID 생성 (appointmentId 기반)
            String taskId = queueName + ":" + taskData.getAppointmentId();
            
            // 실행 시간까지의 지연 시간 계산
            long executeTimestamp = executeAt.toEpochSecond(ZoneOffset.UTC) * 1000;
            long delayMs = executeTimestamp - System.currentTimeMillis();
            
            if (delayMs <= 0) {
                // 이미 시간이 지났으면 즉시 실행
                executeTask(taskData);
                return true;
            }
            
            // 작업 데이터를 별도로 저장
            String taskDataKey = TASK_DATA_PREFIX + taskId;
            String taskJson = objectMapper.writeValueAsString(taskData);
            redisTemplate.opsForValue().set(taskDataKey, taskJson);
            
            // TTL 키 생성 (만료 시 이벤트 발생)
            String delayedKey = DELAYED_TASK_PREFIX + taskId;
            redisTemplate.opsForValue().set(
                delayedKey, 
                queueName, // 큐 이름 저장
                Duration.ofMillis(delayMs)
            );
            
            return true;
            
        } catch (JsonProcessingException e) {
            log.error("작업 데이터 직렬화 실패", e);
            return false;
        }
    }
    
    // Redis 키 만료 이벤트 리스너 설정
    @Bean
    public KeyExpirationEventMessageListener keyExpirationEventMessageListener(
            RedisMessageListenerContainer listenerContainer) {
        
        return new KeyExpirationEventMessageListener(listenerContainer) {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                String expiredKey = message.toString();

                // 지연 작업 키가 아닌 경우 처리하지 않음
                if (!expiredKey.startsWith(DELAYED_TASK_PREFIX)) {
                    return;
                }
                
                // 작업 데이터 조회 및 실행
                try {
                    // 작업 ID 추출
                    String taskId = expiredKey.substring(DELAYED_TASK_PREFIX.length());
                    String taskDataKey = TASK_DATA_PREFIX + taskId;
                    
                    // 작업 데이터 조회
                    String taskJson = (String) redisTemplate.opsForValue().get(taskDataKey);
                    if (taskJson == null) {
                        log.warn("작업 데이터를 찾을 수 없음: {}", taskId);
                        return;
                    }
                    
                    // 작업 실행
                    AppointmentTask taskData = objectMapper.readValue(taskJson, AppointmentTask.class);
                    executeTask(taskData);
                    
                    // 작업 데이터 정리
                    redisTemplate.delete(taskDataKey);
                } catch (Exception e) {
                    log.error("키 만료 처리 중 오류: {}", expiredKey, e);
                }
            }
        };
    }
    
    // 작업 실행 로직
    private void executeTask(AppointmentTask task) {
        switch (task.getTaskType()) {
            case "EXPIRE":
                expireAppointment(task);
                break;
            case "REMIND":
                sendAppointmentReminder(task);
                break;
            default:
                log.warn("알 수 없는 작업 타입: {}", task.getTaskType());
        }
    }
    
    /// 예약 관련 비즈니스 로직 메서드들
    
    // 예약 만료 처리
    private void expireAppointment(AppointmentTask task) {
        // TODO: 예약 만료 처리 로직
    }
    
    // 예약 알림 발송
    private void sendAppointmentReminder(AppointmentTask task) {
        // TODO: 예약 알림 발송 로직
    }
} 