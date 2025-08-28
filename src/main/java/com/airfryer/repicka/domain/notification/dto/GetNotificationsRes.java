package com.airfryer.repicka.domain.notification.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class GetNotificationsRes
{
    private List<NotificationDto> notifications;
    private PageInfo pageInfo;

    public static GetNotificationsRes of(List<NotificationDto> notifications, boolean hasNext, LocalDateTime cursorCreatedAt, Long cursorId)
    {
        return GetNotificationsRes.builder()
                .notifications(notifications)
                .pageInfo(PageInfo.builder()
                        .hasNext(hasNext)
                        .cursorCreatedAt(cursorCreatedAt)
                        .cursorId(cursorId)
                        .build())
                .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class PageInfo
    {
        private Boolean hasNext;                // 다음 페이지 존재 여부
        private LocalDateTime cursorCreatedAt;  // 커서: 알림 생성 날짜
        private Long cursorId;                  // 커서: 알림 ID
    }
}
