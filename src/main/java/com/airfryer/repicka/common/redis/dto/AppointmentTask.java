package com.airfryer.repicka.common.redis.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentType;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AppointmentTask {
    private Long appointmentId; // 예약 아이디
    private AppointmentType transactionType; // 거래 유형
    private Long ownerId; // 소유자 아이디
    private Long requesterId; // 요청자 아이디
    private String taskType; // "EXPIRE", "REMIND"

    public static AppointmentTask from(Appointment appointment, String taskType) {
        return AppointmentTask.builder()
                .appointmentId(appointment.getId())
                .transactionType(appointment.getType())
                .ownerId(appointment.getOwner().getId())
                .requesterId(appointment.getRequester().getId())
                .taskType(taskType)
                .build();
    }
} 