package com.airfryer.repicka.common.firebase.dto;

import com.airfryer.repicka.common.firebase.type.NotificationType;

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
    private Object relatedId;  // appointmentId(Long) 또는 chatId(ObjectId) 등
    private String relatedName;  // 약속 관련 아이템 이름 또는 채팅 상대 이름
    
    // 제목 생성
    public String getTitle() {
        return notificationType.getTitle();
    }
    
    // 내용 생성  
    public String getBody() {
        return notificationType.getFormattedBody(relatedName);
    }

    public static FCMNotificationReq of(NotificationType notificationType, Object id, String name) {
        return FCMNotificationReq.builder()
            .notificationType(notificationType)
            .relatedId(id)
            .relatedName(name)
            .build();
    }
} 