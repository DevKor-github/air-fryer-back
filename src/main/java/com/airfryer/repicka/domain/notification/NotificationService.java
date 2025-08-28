package com.airfryer.repicka.domain.notification;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.notification.dto.GetNotificationsReq;
import com.airfryer.repicka.domain.notification.dto.NotificationDto;
import com.airfryer.repicka.domain.notification.entity.AppointmentJson;
import com.airfryer.repicka.domain.user.entity.user.User;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.notification.dto.GetNotificationsRes;
import com.airfryer.repicka.domain.notification.entity.Notification;
import com.airfryer.repicka.domain.notification.entity.NotificationType;
import com.airfryer.repicka.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService
{
    private final NotificationRepository notificationRepository;
    private final ItemImageService itemImageService;
    
    // 사용자의 알림 목록 조회
    @Transactional(readOnly = true)
    public GetNotificationsRes getNotifications(Long userId, GetNotificationsReq dto)
    {
        /// 알림 페이지 조회

        Pageable pageable = PageRequest.of(0, dto.getPageSize() + 1);
        List<Notification> notificationList;

        if(dto.getCursorCreatedAt() == null || dto.getCursorId() == null) {
            notificationList = notificationRepository.findFirstPageByUserId(userId, pageable);
        } else {
            notificationList = notificationRepository.findPageByUserId(userId, dto.getCursorCreatedAt(), dto.getCursorId(), pageable);
        }

        /// 커서 데이터 계산

        boolean hasNext = notificationList.size() == dto.getPageSize() + 1;
        LocalDateTime cursorCreatedAt = null;
        Long cursorId = null;

        if(hasNext)
        {
            cursorCreatedAt = notificationList.getLast().getCreatedAt();
            cursorId = notificationList.getLast().getId();
            notificationList = new ArrayList<>(notificationList.subList(0, dto.getPageSize()));
        }

        /// 알림 페이지를 NotificationDto 리스트로 변환

        List<NotificationDto> notificationDtoList = notificationList.stream()
            .map(notification -> NotificationDto.from(notification, itemImageService.getThumbnail(notification.getItem())))
            .toList();

        /// 데이터 반환

        return GetNotificationsRes.of(notificationDtoList, hasNext, cursorCreatedAt, cursorId);
    }

    // 알림 저장
    @Transactional
    public void saveNotification(User user, NotificationType type, Appointment appointment)
    {
        Notification notification = Notification.builder()
            .user(user)
            .type(type)
            .appointment(AppointmentJson.from(appointment))
            .item(appointment.getItem())
            .build();

        notificationRepository.save(notification);
    }
}
