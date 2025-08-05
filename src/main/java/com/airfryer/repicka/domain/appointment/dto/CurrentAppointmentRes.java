package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class CurrentAppointmentRes
{
    private Boolean isPresent;              // 완료되지 않은 약속 존재 여부
    private Long chatRoomId;                // 채팅방 ID
    private AppointmentRes appointment;     // 약속 정보

    public static CurrentAppointmentRes from(boolean isPresent, Appointment appointment, ChatRoom chatRoom)
    {
        return CurrentAppointmentRes.builder()
                .isPresent(isPresent)
                .chatRoomId(isPresent ? chatRoom.getId() : null)
                .appointment(isPresent ? AppointmentRes.createPreview(appointment) : null)
                .build();
    }

    public static CurrentAppointmentRes from(boolean isPresent, Appointment appointment)
    {
        return CurrentAppointmentRes.builder()
                .isPresent(isPresent)
                .chatRoomId(null)
                .appointment(isPresent ? AppointmentRes.createPreview(appointment) : null)
                .build();
    }
}
