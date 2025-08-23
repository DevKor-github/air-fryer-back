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
    
    // 제목 생성
    public String getTitle() {
        return notificationType.getTitle();
    }
    
    // 내용 생성  
    public String getBody() {
        return notificationType.getFormattedBody(relatedName);
    }

    public static FCMNotificationReq of(NotificationType notificationType, String id, String name) {
        return FCMNotificationReq.builder()
            .notificationType(notificationType)
            .relatedId(id)
            .relatedName(name)
            .build();
    }
} 