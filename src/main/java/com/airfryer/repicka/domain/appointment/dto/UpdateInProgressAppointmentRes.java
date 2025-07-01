package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.UpdateInProgressAppointment;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UpdateInProgressAppointmentRes
{
    private Long appointmentId;                 // 약속 ID
    private LocalDateTime originalReturnDate;   // 기존의 반납 일시
    private String originalReturnLocation;      // 기존의 반납 장소
    private LocalDateTime newReturnDate;        // 새로운 반납 일시
    private String newReturnLocation;           // 새로운 반납 장소

    public static UpdateInProgressAppointmentRes from(Appointment appointment, UpdateInProgressAppointment updateInProgressAppointment)
    {
        return UpdateInProgressAppointmentRes.builder()
                .appointmentId(appointment.getId())
                .originalReturnDate(appointment.getReturnDate())
                .originalReturnLocation(appointment.getReturnLocation())
                .newReturnDate(updateInProgressAppointment.getReturnDate())
                .newReturnLocation(updateInProgressAppointment.getReturnLocation())
                .build();
    }
}
