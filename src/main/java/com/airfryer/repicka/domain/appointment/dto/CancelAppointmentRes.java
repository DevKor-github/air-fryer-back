package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.post.entity.PostType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CancelAppointmentRes
{
    private Long appointmentId;     // 약속 ID
    private Long postId;            // 게시글 ID
    private Long ownerId;           // 소유자 ID
    private Long borrowerId;        // 대여자(구매자) ID

    private PostType type;              // 약속 종류
    private LocalDateTime rentalDate;   // 대여(구매) 일시
    private LocalDateTime returnDate;   // 반납 일시
    private String rentalLocation;      // 대여(구매) 장소
    private String returnLocation;      // 반납 장소
    private int price;                  // 대여료(판매값)
    private int deposit;                // 보증금
}
