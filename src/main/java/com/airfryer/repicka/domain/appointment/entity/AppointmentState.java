package com.airfryer.repicka.domain.appointment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AppointmentState
{
    PENDING("PENDING", "협의중"),
    CONFIRMED("CONFIRMED", "확정"),
    CANCELLED("CANCELLED", "취소"),
    EXPIRED("EXPIRED", "만료"),
    IN_PROGRESS("IN_PROGRESS", "대여중"),
    SUCCESS("SUCCESS", "완료");

    private final String code;
    private final String label;
}
