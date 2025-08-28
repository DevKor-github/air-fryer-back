package com.airfryer.repicka.domain.notification.dto;

import com.airfryer.repicka.domain.item.dto.ItemPreviewDto;
import com.airfryer.repicka.domain.notification.entity.AppointmentJson;
import com.airfryer.repicka.domain.notification.entity.Notification;
import com.airfryer.repicka.domain.notification.entity.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class NotificationDto
{
    private NotificationInfo notificationInfo;  // 알림 정보
    private AppointmentInfo appointmentInfo;    // 약속 정보
    private ItemPreviewDto itemInfo;            // 제품 정보

    public static NotificationDto from(Notification notification, String itemThumbnailUrl)
    {
        return NotificationDto.builder()
                .notificationInfo(NotificationInfo.from(notification))
                .appointmentInfo(notification.getAppointment() != null ? AppointmentInfo.from(notification.getAppointment()) : null)
                .itemInfo(notification.getItem() != null ? ItemPreviewDto.from(notification.getItem(), itemThumbnailUrl) : null)
                .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class NotificationInfo
    {
        private Long id;                    // 알림 ID
        private NotificationType type;      // 알림 타입
        private LocalDateTime createdAt;    // 알림 생성 시간

        private static NotificationInfo from(Notification notification)
        {
            return NotificationInfo.builder()
                    .id(notification.getId())
                    .type(notification.getType())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class AppointmentInfo
    {
        private Long id;                    // 약속 ID
        private LocalDateTime rentalDate;   // 약속 대여(구매) 일시
        private LocalDateTime returnDate;   // 약속 반납 일시

        private static AppointmentInfo from(AppointmentJson appointment)
        {
            return AppointmentInfo.builder()
                    .id(appointment.getId())
                    .rentalDate(appointment.getRentalDate())
                    .returnDate(appointment.getReturnDate())
                    .build();
        }
    }
}
