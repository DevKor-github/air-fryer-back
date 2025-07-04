package com.airfryer.repicka.domain.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OfferAppointmentInSalePostReq
{
    @NotNull(message = "게시물 ID를 입력해주세요.")
    private Long postId;

    @Size(max = 255, message = "구매 장소는 최대 255자까지 입력할 수 있습니다.")
    private String saleLocation;      // 구매 장소

    @NotNull(message = "구매 일시를 입력해주세요.")
    @Future(message = "구매 일시는 현재보다 미래여야 합니다.")
    private LocalDateTime saleDate;   // 구매 일시

    @NotNull(message = "판매값을 입력해주세요.")
    @Min(value = 0, message = "판매값은 0원 이상이어야 합니다.")
    private int price;  // 판매값
}
