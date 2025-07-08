package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.appointment.FindMyAppointmentPeriod;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.item.entity.PostType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FindMyAppointmentPageReq
{
    @NotNull(message = "약속 타입을 입력해주세요. (RENTAL / SALE)")
    private PostType type;

    @NotNull(message = "검색 기간을 입력해주세요. (ALL / YEAR / SIX_MONTH / WEEK)")
    private FindMyAppointmentPeriod period;

    @NotNull(message = "페이지 크기를 입력해주세요.")
    @Positive(message = "페이지 크기는 0보다 커야 합니다.")
    private int pageSize;

    // 커서 데이터
    private AppointmentState cursorState;   // 약속 상태
    private LocalDateTime cursorDate;       // 대여(구매) 일시
    private Long cursorId;                  // 약속 ID
}
