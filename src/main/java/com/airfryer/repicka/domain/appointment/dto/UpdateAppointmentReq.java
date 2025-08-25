package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.item.entity.TradeMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UpdateAppointmentReq
{
    @NotNull(message = "약속 ID를 입력해주세요.")
    private Long appointmentId;

    @Size(max = 255, message = "대여 장소는 최대 255자까지 입력할 수 있습니다.")
    private String rentalLocation;

    @Size(max = 255, message = "반납 장소는 최대 255자까지 입력할 수 있습니다.")
    private String returnLocation = null;

    @NotNull(message = "대여 일시를 입력해주세요.")
    @Future(message = "대여 일시는 현재보다 미래여야 합니다.")
    private LocalDateTime rentalDate;

    @Future(message = "반납 일시는 현재보다 미래여야 합니다.")
    private LocalDateTime returnDate = null;

    @NotNull(message = "대여료(판매값)를 입력해주세요.")
    @Min(value = 0, message = "대여료(판매값)는 0원 이상이어야 합니다.")
    private int price;

    @Min(value = 0, message = "보증금은 0원 이상이어야 합니다.")
    private Integer deposit;

    @NotNull(message = "거래 방식을 입력해주세요.")
    private TradeMethod tradeMethod;
}
