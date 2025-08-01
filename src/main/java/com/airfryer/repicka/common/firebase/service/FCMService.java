package com.airfryer.repicka.common.firebase.service;

import com.google.firebase.messaging.*;
import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    // 단일 기기에 푸시 알림 전송
    public void sendNotification(String token, FCMNotificationReq request) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build())
                    .putData("notificationType", request.getNotificationType().name())
                    .putData("relatedId", request.getRelatedId())
                    .build();

            firebaseMessaging.send(message);

        } catch (FirebaseMessagingException e) {
            log.error("푸시 알림 전송 실패: {}", e.getMessage(), e);
        }
    }

    // 여러 기기에 푸시 알림 전송
    public void sendNotificationToMultiple(List<String> tokens, FCMNotificationReq request) {
        // 존재하는 토큰만 필터링
        tokens = tokens.stream()
                .filter(token -> token != null && !token.isEmpty())
                .toList();
        
        if (tokens.isEmpty()) {
            return;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build())
                    .putData("notificationType", request.getNotificationType().name())
                    .putData("relatedId", request.getRelatedId())
                    .build();

            firebaseMessaging.sendEachForMulticast(message);

        } catch (FirebaseMessagingException e) {
            log.error("멀티캐스트 푸시 알림 전송 실패: {}", e.getMessage(), e);
        }
    }
} 