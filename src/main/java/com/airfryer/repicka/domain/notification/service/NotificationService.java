package com.airfryer.repicka.domain.notification.service;

import com.airfryer.repicka.domain.item.dto.ItemPreviewDto;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.notification.dto.NotificationRes;
import com.airfryer.repicka.domain.notification.entity.Notification;
import com.airfryer.repicka.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final ItemImageService itemImageService;
    
    /**
     * 사용자의 알림 목록 조회
     * @param userId 사용자 ID
     * @return 알림 목록
     */
    public List<NotificationRes> getNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return notifications.stream()
                .map(this::convertToNotificationRes)
                .collect(Collectors.toList());
    }
    
    /**
     * Notification 엔티티를 NotificationRes DTO로 변환
     */
    private NotificationRes convertToNotificationRes(Notification notification) {
        ItemPreviewDto itemPreviewDto = null;
        
        // 아이템이 있는 경우 ItemPreviewDto 생성
        if (notification.getItem() != null) {
            String thumbnailUrl = itemImageService.getThumbnail(notification.getItem());
            itemPreviewDto = ItemPreviewDto.from(notification.getItem(), thumbnailUrl);
        }
        
        Long appointmentId = null;
        if (notification.getAppointment() != null) {
            appointmentId = notification.getAppointment().getId();
        }
        
        return NotificationRes.builder()
                .notificationId(notification.getId())
                .item(itemPreviewDto)
                .appointmentId(appointmentId)
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}