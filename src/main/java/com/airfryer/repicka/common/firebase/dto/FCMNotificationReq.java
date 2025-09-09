package com.airfryer.repicka.common.firebase.dto;

import com.airfryer.repicka.domain.notification.entity.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FCMNotificationReq {
    private NotificationType notificationType;
    private String relatedId;  // appointmentId, chatId 등
    private String relatedName;  // 약속 관련 아이템 이름 또는 채팅 상대 이름

    private String customTitle;
    private String customBody;

    public String getTitle() {
        return customTitle != null ? customTitle : notificationType.getTitle();
    }

    public String getBody() {
        return customBody != null ? customBody : notificationType.getFormattedBody(relatedName);
    }

    public static FCMNotificationReq of(NotificationType notificationType, String id, String name)
    {
        return FCMNotificationReq.builder()
            .notificationType(notificationType)
            .relatedId(id)
            .relatedName(name)
            .build();
    }

    // 채팅 메시지 전용 팩토리 메서드
    public static FCMNotificationReq of(String chatId, String customTitle, String customBody)
    {
        return FCMNotificationReq.builder()
                .notificationType(NotificationType.CHAT_MESSAGE)
                .relatedId(chatId)
                .customTitle(customTitle)
                .customBody(customBody)
                .build();
    }
} 