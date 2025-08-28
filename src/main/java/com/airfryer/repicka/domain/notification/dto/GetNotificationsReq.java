package com.airfryer.repicka.domain.notification.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class GetNotificationsReq
{
    @NotNull(message = "페이지 크기를 입력해주세요.")
    @Positive(message = "페이지 크기는 0보다 커야 합니다.")
    private int pageSize;

    // 커서 데이터
    private LocalDateTime cursorCreatedAt;  // 알림 생성 시간
    private Long cursorId;                  // 알림 ID
}
