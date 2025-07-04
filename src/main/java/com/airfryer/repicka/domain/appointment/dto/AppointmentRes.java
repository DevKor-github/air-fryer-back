package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.UpdateInProgressAppointment;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class AppointmentRes
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

    public static AppointmentRes from(Appointment appointment, Post post)
    {
        return AppointmentRes.builder()
                .appointmentId(appointment.getId())
                .postId(post.getId())
                .ownerId(appointment.getOwner().getId())
                .borrowerId(appointment.getRequester().getId())
                .type(post.getPostType())
                .rentalDate(appointment.getRentalDate())
                .returnDate(appointment.getReturnDate())
                .rentalLocation(appointment.getRentalLocation())
                .returnLocation(appointment.getReturnLocation())
                .price(appointment.getPrice())
                .deposit(appointment.getDeposit())
                .build();
    }

    public static AppointmentRes from(UpdateInProgressAppointment updateInProgressAppointment, Post post)
    {
        return AppointmentRes.builder()
                .appointmentId(updateInProgressAppointment.getAppointment().getId())
                .postId(post.getId())
                .ownerId(updateInProgressAppointment.getAppointment().getOwner().getId())
                .borrowerId(updateInProgressAppointment.getAppointment().getRequester().getId())
                .type(post.getPostType())
                .rentalDate(updateInProgressAppointment.getAppointment().getRentalDate())
                .returnDate(updateInProgressAppointment.getReturnDate())
                .rentalLocation(updateInProgressAppointment.getAppointment().getRentalLocation())
                .returnLocation(updateInProgressAppointment.getReturnLocation())
                .price(updateInProgressAppointment.getAppointment().getPrice())
                .deposit(updateInProgressAppointment.getAppointment().getDeposit())
                .build();
    }
}
