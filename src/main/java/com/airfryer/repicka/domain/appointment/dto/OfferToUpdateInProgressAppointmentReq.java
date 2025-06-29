package com.airfryer.repicka.domain.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OfferToUpdateInProgressAppointmentReq
{
    @NotNull(message = "약속 ID를 입력해주세요.")
    private Long appointmentId;

    @Size(max = 255, message = "반납 장소는 최대 255자까지 입력할 수 있습니다.")
    private String returnLocation;

    @NotNull(message = "반납 일시를 입력해주세요.")
    @Future(message = "반납 일시는 현재보다 미래여야 합니다.")
    private LocalDateTime returnDate;
}
