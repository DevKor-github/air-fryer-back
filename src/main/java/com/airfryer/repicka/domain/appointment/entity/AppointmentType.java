package com.airfryer.repicka.domain.appointment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AppointmentType
{
    RENTAL("RENTAL", "대여"),
    SALE("SALE", "구매");

    private final String code;
    private final String label;
}
