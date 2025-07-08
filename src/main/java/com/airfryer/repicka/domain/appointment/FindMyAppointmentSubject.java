package com.airfryer.repicka.domain.appointment;

import com.airfryer.repicka.domain.appointment.dto.FindMyAppointmentPageReq;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
public enum FindMyAppointmentSubject
{
    REQUESTER("REQUESTER", "대여자(구매자)") {
        @Override
        public List<Appointment> findAppointmentPage(AppointmentRepository repository,
                                                     User user,
                                                     FindMyAppointmentPageReq dto)
        {
            if(dto.getCursorState() == null || dto.getCursorDate() == null || dto.getCursorId() == null)
            {
                return repository.findMyAppointmentFirstPageAsRequester(
                        user.getId(),
                        dto.getType().name(),
                        dto.getPeriod().calculateFromDate(LocalDateTime.now()),
                        dto.getPageSize() + 1
                );
            }
            else
            {
                return repository.findMyAppointmentPageAsRequester(
                        user.getId(),
                        dto.getType().name(),
                        dto.getPeriod().calculateFromDate(LocalDateTime.now()),
                        dto.getCursorState().name(),
                        dto.getCursorDate(),
                        dto.getCursorId(),
                        dto.getPageSize() + 1
                );
            }
        }
    },
    OWNER("OWNER", "소유자") {
        @Override
        public List<Appointment> findAppointmentPage(AppointmentRepository repository,
                                                     User user,
                                                     FindMyAppointmentPageReq dto)
        {
            if(dto.getCursorState() == null || dto.getCursorDate() == null || dto.getCursorId() == null)
            {
                return repository.findMyAppointmentFirstPageAsOwner(
                        user.getId(),
                        dto.getType().name(),
                        dto.getPeriod().calculateFromDate(LocalDateTime.now()),
                        dto.getPageSize() + 1
                );
            }
            else
            {
                return repository.findMyAppointmentPageAsOwner(
                        user.getId(),
                        dto.getType().name(),
                        dto.getPeriod().calculateFromDate(LocalDateTime.now()),
                        dto.getCursorState().name(),
                        dto.getCursorDate(),
                        dto.getCursorId(),
                        dto.getPageSize() + 1
                );
            }
        }
    };

    private final String code;
    private final String label;

    public abstract List<Appointment> findAppointmentPage(AppointmentRepository repository,
                                                          User user,
                                                          FindMyAppointmentPageReq dto);
}
