package com.airfryer.repicka.domain.appointment.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OfferAppointmentInPostReq
{
    @NotNull(message = "게시물 ID를 입력해주세요.")
    private Long postId;

    @Size(max = 255, message = "대여 장소는 최대 255자까지 입력할 수 있습니다.")
    private String rentalLocation;      // 대여 장소

    @Size(max = 255, message = "반납 장소는 최대 255자까지 입력할 수 있습니다.")
    private String returnLocation;      // 반납 장소

    @NotNull(message = "대여 일시를 입력해주세요.")
    @Future(message = "대여 일시는 현재보다 미래여야 합니다.")
    private LocalDateTime rentalDate;   // 대여 일시

    @NotNull(message = "반납 일시를 입력해주세요.")
    @Future(message = "반납 일시는 현재보다 미래여야 합니다.")
    private LocalDateTime returnDate;   // 반납 일시

    @NotNull(message = "대여료 또는 판매값을 입력해주세요.")
    @Min(value = 0, message = "대여료 또는 판매값은 0원 이상이어야 합니다.")
    private int price;      // 대여료/판매값

    @NotNull(message = "보증금을 입력해주세요.")
    @Min(value = 0, message = "보증금은 0원 이상이어야 합니다.")
    private int deposit = 0;    // 보증금
}
