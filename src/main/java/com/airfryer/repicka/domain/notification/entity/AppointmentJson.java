package com.airfryer.repicka.domain.notification.entity;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AppointmentJson
{
    private Long id;
    private LocalDateTime rentalDate;
    private LocalDateTime returnDate;

    public static AppointmentJson from(Appointment appointment)
    {
        return AppointmentJson.builder()
                .id(appointment.getId())
                .rentalDate(appointment.getRentalDate())
                .returnDate(appointment.getReturnDate())
                .build();
    }
}
