package com.airfryer.repicka.domain.notification.dto;

import com.airfryer.repicka.domain.notification.entity.NotificationType;
import com.airfryer.repicka.domain.item.dto.ItemPreviewDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationRes
{
    private Long notificationId;        // 알림 ID
    private ItemPreviewDto item;        // 아이템 정보
    private Long appointmentId;         // 약속 ID
    private LocalDateTime rentalDate;   // 약속 대여(구매) 일시
    private NotificationType type;      // 알림 타입
    private LocalDateTime createdAt;    // 알림 생성 시간
}
