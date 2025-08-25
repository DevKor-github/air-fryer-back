package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class AppointmentPageRes
{
    private List<AppointmentInfo> appointmentInfoList;  // 약속 정보 리스트
    private PageInfo pageInfo;                          // 페이지 정보

    public static AppointmentPageRes of(
            List<AppointmentInfo> appointmentInfoList,
            AppointmentState cursorState,
            LocalDateTime cursorDate,
            Long cursorId,
            Boolean hasNext)
    {
        return AppointmentPageRes.builder()
                .appointmentInfoList(appointmentInfoList)
                .pageInfo(PageInfo.builder()
                        .cursorState(cursorState)
                        .cursorDate(cursorDate)
                        .cursorId(cursorId)
                        .hasNext(hasNext)
                        .build())
                .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class PageInfo
    {
        // 커서
        private AppointmentState cursorState;   // 약속 상태
        private LocalDateTime cursorDate;       // 대여(구매) 일시
        private Long cursorId;                  // 약속 ID

        private Boolean hasNext;    // 다음 페이지가 존재하는가?
    }
}
