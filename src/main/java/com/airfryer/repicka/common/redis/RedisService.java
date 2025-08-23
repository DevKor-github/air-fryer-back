package com.airfryer.repicka.common.redis;

import com.airfryer.repicka.common.redis.dto.AppointmentTask;
import com.airfryer.repicka.common.redis.dto.KeyExpiredEvent;
import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;
import com.airfryer.repicka.common.firebase.type.NotificationType;
import com.airfryer.repicka.common.firebase.service.FCMService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final FCMService fcmService;
    private static final String DELAYED_TASK_PREFIX = "delayed:task:";
    private static final String TASK_DATA_PREFIX = "taskdata:";
    
    // 키 쌍을 담는 record
    public record TaskKeys(String taskDataKey, String delayedKey) {}
    
    // 키 생성 유틸리티 메서드
    private TaskKeys generateTaskKeys(String queueName, Long appointmentId) {
        String taskId = queueName + ":" + appointmentId;
        return new TaskKeys(
            TASK_DATA_PREFIX + taskId,
            DELAYED_TASK_PREFIX + taskId
        );
    }
    
    // Redis 키 만료 이벤트 처리
    @EventListener
    public void handleKeyExpired(KeyExpiredEvent event) {
        String expiredKey = event.getExpiredKey();

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

    // 지연 작업을 Redis TTL로 등록
    public boolean addDelayedTask(String queueName, AppointmentTask taskData, LocalDateTime executeAt) {
        try {
            // 필요한 키들 생성
            TaskKeys keys = generateTaskKeys(queueName, taskData.getAppointmentId());
            
            // 실행 시간까지의 지연 시간 계산
            long executeTimestamp = executeAt.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
            long delayMs = executeTimestamp - System.currentTimeMillis();
            
            if (delayMs <= 0) {
                // 이미 시간이 지났으면 즉시 실행
                executeTask(taskData);
                return true;
            }
            
            // 작업 데이터를 별도로 저장
            String taskJson = objectMapper.writeValueAsString(taskData);
            redisTemplate.opsForValue().set(keys.taskDataKey(), taskJson);
            
            // TTL 키 생성 (만료 시 이벤트 발생)
            redisTemplate.opsForValue().set(
                keys.delayedKey(), 
                queueName, // 큐 이름 저장
                Duration.ofMillis(delayMs)
            );
            
            return true;
            
        } catch (JsonProcessingException e) {
            log.error("작업 데이터 직렬화 실패", e);
            return false;
        }
    }

    // 지연 작업 취소
    public boolean cancelDelayedTask(String queueName, Long appointmentId) {
        TaskKeys keys = generateTaskKeys(queueName, appointmentId);

        // 작업 데이터 정리
        redisTemplate.delete(keys.taskDataKey());
        redisTemplate.delete(keys.delayedKey());

        return true;
    }
    
    // 작업 실행 로직
    private void executeTask(AppointmentTask task) {
        switch (task.getTaskType()) {
            case EXPIRE:
                expireAppointment(task);
                break;
            case REMIND:
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
        FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_REMINDER, task.getAppointmentId().toString(), task.getItemName());
        List<String> fcmTokens = Arrays.asList(task.getOwnerFcmToken(), task.getRequesterFcmToken());
        fcmService.sendNotificationToMultiple(fcmTokens, notificationReq);
    }
} 