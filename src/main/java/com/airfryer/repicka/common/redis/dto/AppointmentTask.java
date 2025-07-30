package com.airfryer.repicka.common.redis.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.airfryer.repicka.common.redis.type.TaskType;
import com.airfryer.repicka.domain.appointment.entity.Appointment;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AppointmentTask {
    private Long appointmentId; // 예약 아이디
    private String itemName; // 아이템 이름
    private String ownerFcmToken; // 소유자 FCM 토큰
    private String requesterFcmToken; // 요청자 FCM 토큰
    private TaskType taskType; // 작업 타입 (EXPIRE, REMIND)

    public static AppointmentTask from(Appointment appointment, TaskType taskType) {
        return AppointmentTask.builder()
                .appointmentId(appointment.getId())
                .itemName(appointment.getItem().getTitle())
                .ownerFcmToken(appointment.getOwner().getFcmToken())
                .requesterFcmToken(appointment.getRequester().getFcmToken())
                .taskType(taskType)
                .build();
    }
} 