package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.entity.ProductType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class AppointmentInfo
{
    private Long appointmentId;     // 약속 ID
    private Long itemId;            // 제품 ID
    private Long requesterId;       // 대여자(구매자) ID
    private Long ownerId;           // 소유자 ID

    private String imageUrl;            // 이미지 URL
    private String title;               // 게시글 제목
    private String description;         // 게시글 설명
    private ProductType[] productTypes; // 제품 타입
    private LocalDateTime rentalDate;   // 대여(구매) 일시
    private LocalDateTime returnDate;   // 반납 일시
    private String rentalLocation;      // 대여(구매) 장소
    private String returnLocation;      // 반납 장소
    private int price;                  // 대여료(판매값)
    private int deposit;                // 보증금
    private AppointmentState state;     // 약속 상태

    public static AppointmentInfo from(Appointment appointment, Optional<String> imageUrl)
    {
        Item item = appointment.getItem();

        return AppointmentInfo.builder()
                .appointmentId(appointment.getId())
                .itemId(item.getId())
                .requesterId(appointment.getRequester().getId())
                .ownerId(appointment.getOwner().getId())
                .imageUrl(imageUrl.orElse(null))
                .title(item.getTitle())
                .description(item.getDescription())
                .productTypes(item.getProductTypes())
                .rentalDate(appointment.getRentalDate())
                .returnDate(appointment.getReturnDate())
                .rentalLocation(appointment.getRentalLocation())
                .returnLocation(appointment.getReturnLocation())
                .price(appointment.getPrice())
                .deposit(appointment.getDeposit())
                .state(appointment.getState())
                .build();
    }
}
