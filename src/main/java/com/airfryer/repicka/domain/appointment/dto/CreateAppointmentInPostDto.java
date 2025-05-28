package com.airfryer.repicka.domain.appointment.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateAppointmentInPostDto
{
    private String rentalLocation;      // 대여 장소
    private String returnLocation;      // 반납 장소
    private LocalDateTime rentalDate;   // 대여 일시
    private LocalDateTime returnDate;   // 반납 일시
    private int price;                  // 대여료/판매값
    private int deposit;                // 보증금
}
