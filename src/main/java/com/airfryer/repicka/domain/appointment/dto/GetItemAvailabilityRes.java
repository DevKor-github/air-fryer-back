package com.airfryer.repicka.domain.appointment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class GetItemAvailabilityRes
{
    private Long itemId;
    private int year;
    private int month;
    private Map<LocalDate, Boolean> availability;
}
