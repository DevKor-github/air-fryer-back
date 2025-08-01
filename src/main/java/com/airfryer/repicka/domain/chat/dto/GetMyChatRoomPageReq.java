package com.airfryer.repicka.domain.chat.dto;

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
public class GetMyChatRoomPageReq
{
    @NotNull(message = "페이지 크기를 입력해주세요.")
    @Positive(message = "페이지 크기는 0보다 커야 합니다.")
    private int pageSize;

    // 커서 데이터
    private LocalDateTime cursorCreatedAt;  // 채팅방 생성 날짜
    private Long cursorId;                  // 채팅방 ID
}
