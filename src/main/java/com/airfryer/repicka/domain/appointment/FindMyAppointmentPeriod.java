package com.airfryer.repicka.domain.appointment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.Period;

@Getter
@AllArgsConstructor
public enum FindMyAppointmentPeriod
{
    ALL("ALL", "전체", Period.ofYears(100)),
    YEAR("YEAR", "1년", Period.ofYears(1)),
    SIX_MONTH("SIX_MONTH", "6개월", Period.ofMonths(6)),
    WEEK("WEEK", "일주일", Period.ofWeeks(1));

    private final String code;
    private final String label;
    private final Period period;

    // 특정 기준 날짜로부터 기간을 뺀 날짜를 반환
    public LocalDateTime calculateFromDate(LocalDateTime baseDate) {
        return baseDate.minus(period);
    }
}
