package com.airfryer.repicka.domain.notification;

import com.airfryer.repicka.domain.item.dto.ItemPreviewDto;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.notification.dto.NotificationRes;
import com.airfryer.repicka.domain.notification.entity.Notification;
import com.airfryer.repicka.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final ItemImageService itemImageService;
    
    // 사용자의 알림 목록 조회
    public List<NotificationRes> getNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return notifications.stream()
            .map(this::convertToNotificationRes)
            .toList();
    }

    private NotificationRes convertToNotificationRes(Notification notification) {
        return NotificationRes.builder()
            .notificationId(notification.getId())
            .item(notification.getItem() != null ? 
                ItemPreviewDto.from(notification.getItem(), itemImageService.getThumbnail(notification.getItem())) 
                : null)
            .appointmentId(notification.getAppointment() != null ? 
                notification.getAppointment().getId() 
                : null)
            .type(notification.getType())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
